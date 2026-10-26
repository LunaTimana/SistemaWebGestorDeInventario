package com.emsstore.backend.service;

import com.emsstore.backend.dto.ImportacionResponse;
import com.emsstore.backend.model.Categoria;
import com.emsstore.backend.model.EstadoUsuario;
import com.emsstore.backend.model.Marca;
import com.emsstore.backend.model.MotivoMovimiento;
import com.emsstore.backend.model.MovimientoInventario;
import com.emsstore.backend.model.Producto;
import com.emsstore.backend.model.TipoMovimiento;
import com.emsstore.backend.model.Usuario;
import com.emsstore.backend.model.VarianteProducto;
import com.emsstore.backend.repository.CategoriaRepository;
import com.emsstore.backend.repository.MarcaRepository;
import com.emsstore.backend.repository.MovimientoInventarioRepository;
import com.emsstore.backend.repository.ProductoRepository;
import com.emsstore.backend.repository.UsuarioRepository;
import com.emsstore.backend.repository.VarianteProductoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class ImportacionStockService {

    // El Excel no trae color ni stock mínimo
    private static final String COLOR_POR_DEFECTO = "Único";
    private static final int STOCK_MINIMO_POR_DEFECTO = 0;
    private static final String DOCUMENTO = "Migración Excel";

    private record Precio(BigDecimal costo, BigDecimal venta) {
    }

    // PRECIOS INVENTADOS para poder probar la migración. Reemplázalos por los reales.
    private static final Map<String, Precio> PRECIOS_PROVISIONALES = Map.of(
            "Colección Alianza", new Precio(new BigDecimal("45.00"), new BigDecimal("89.90")),
            "Colección Universitario", new Precio(new BigDecimal("45.00"), new BigDecimal("89.90")),
            "Internacionales", new Precio(new BigDecimal("50.00"), new BigDecimal("99.90")),
            "Internacionales Retro", new Precio(new BigDecimal("55.00"), new BigDecimal("109.90")),
            "Niños", new Precio(new BigDecimal("35.00"), new BigDecimal("69.90")));

    private final ExcelStockReader lector;
    private final MarcaRepository marcas;
    private final CategoriaRepository categorias;
    private final ProductoRepository productos;
    private final VarianteProductoRepository variantes;
    private final MovimientoInventarioRepository movimientos;
    private final UsuarioRepository usuarios;

    public ImportacionStockService(ExcelStockReader lector,
                                   MarcaRepository marcas,
                                   CategoriaRepository categorias,
                                   ProductoRepository productos,
                                   VarianteProductoRepository variantes,
                                   MovimientoInventarioRepository movimientos,
                                   UsuarioRepository usuarios) {
        this.lector = lector;
        this.marcas = marcas;
        this.categorias = categorias;
        this.productos = productos;
        this.variantes = variantes;
        this.movimientos = movimientos;
        this.usuarios = usuarios;
    }

    // Con confirmar = false solo calcula y informa; con true guarda todo en UNA transacción:
    // si algo falla, no se importa nada.
    @Transactional
    public ImportacionResponse procesar(MultipartFile archivo, Integer responsableId, boolean confirmar) {
        if (archivo == null || archivo.isEmpty()) {
            throw invalido("Selecciona un archivo Excel (.xlsx)");
        }
        String nombreArchivo = archivo.getOriginalFilename();
        if (nombreArchivo == null || !nombreArchivo.toLowerCase(Locale.ROOT).endsWith(".xlsx")) {
            throw invalido("El archivo debe ser un Excel con extensión .xlsx");
        }

        Usuario responsable = null;
        if (confirmar) {
            if (responsableId == null) {
                throw invalido("Indica el responsable de la importación");
            }
            responsable = usuarios.findById(responsableId)
                    .filter(u -> u.getEstado() == EstadoUsuario.activo)
                    .orElseThrow(() -> invalido("El responsable no existe o está inactivo"));
        }

        ExcelStockReader.Lectura lectura;
        try (InputStream entrada = archivo.getInputStream()) {
            lectura = lector.leer(entrada);
        } catch (IOException | RuntimeException e) {
            throw invalido("No se pudo leer el archivo Excel: " + e.getMessage());
        }

        List<String> advertencias = new ArrayList<>(lectura.advertencias());
        advertencias.add(0, "Los costos y precios son PROVISIONALES (inventados); cámbialos antes de usar los reportes.");
        advertencias.add(1, "El color de todas las variantes es \"" + COLOR_POR_DEFECTO
                + "\" y el stock mínimo es " + STOCK_MINIMO_POR_DEFECTO + " (las alertas no se activarán).");

        Set<String> skusUsados = new HashSet<>();
        int productosNuevos = 0;
        int variantesNuevas = 0;
        int variantesOmitidas = 0;
        long unidades = 0;

        for (ExcelStockReader.ModeloLeido modelo : lectura.modelos()) {
            String marcaNombre = detectarMarca(modelo.nombre());
            Marca marca = buscarOCrearMarca(marcaNombre, confirmar);
            Categoria categoria = buscarOCrearCategoria(modelo.categoria(), confirmar);

            // En la vista previa la marca o la categoría pueden no existir todavía: el producto es nuevo
            Producto producto = null;
            if (marca != null && categoria != null) {
                producto = productos
                        .findFirstByMarcaIdAndCategoriaIdAndNombreModeloIgnoreCase(
                                marca.getId(), categoria.getId(), modelo.nombre())
                        .orElse(null);
            }
            if (producto == null) {
                productosNuevos++;
                if (confirmar) {
                    Precio precio = PRECIOS_PROVISIONALES.get(modelo.categoria());
                    Producto nuevo = new Producto();
                    nuevo.setNombreModelo(modelo.nombre());
                    nuevo.setMarca(marca);
                    nuevo.setCategoria(categoria);
                    nuevo.setCostoUnitario(precio.costo());
                    nuevo.setPrecioVenta(precio.venta());
                    producto = productos.save(nuevo);
                }
            }

            for (Map.Entry<String, Integer> par : modelo.stockPorTalla().entrySet()) {
                String talla = par.getKey();
                int cantidad = par.getValue();

                // Al repetir la importación, lo que ya existe se omite en lugar de duplicarse
                if (producto != null
                        && variantes.existsByProductoIdAndTallaAndColor(producto.getId(), talla, COLOR_POR_DEFECTO)) {
                    variantesOmitidas++;
                    continue;
                }

                String base = SkuGenerator.generar(marcaNombre, modelo.nombre(), talla, COLOR_POR_DEFECTO);
                String sku = base;
                int sufijo = 2;
                while (skusUsados.contains(sku) || variantes.existsByCodigoUnico(sku)) {
                    sku = base + "-" + sufijo++;
                }
                skusUsados.add(sku);
                if (!sku.equals(base)) {
                    advertencias.add(String.format(
                            "SKU repetido para \"%s\" talla %s: se usó %s.", modelo.nombre(), talla, sku));
                }

                if (confirmar) {
                    VarianteProducto variante = new VarianteProducto();
                    variante.setProducto(producto);
                    variante.setTalla(talla);
                    variante.setColor(COLOR_POR_DEFECTO);
                    variante.setCodigoUnico(sku);
                    variante.setStockActual(cantidad);
                    variante.setStockMinimo(STOCK_MINIMO_POR_DEFECTO);
                    variante = variantes.save(variante);

                    // El saldo inicial queda como movimiento, igual que al registrar un producto
                    MovimientoInventario movimiento = new MovimientoInventario();
                    movimiento.setVariante(variante);
                    movimiento.setTipo(TipoMovimiento.entrada);
                    movimiento.setMotivo(MotivoMovimiento.compra);
                    movimiento.setCantidad(cantidad);
                    movimiento.setUsuario(responsable);
                    movimiento.setDocumento(DOCUMENTO);
                    movimiento.setComentario("Saldo inicial por migración desde Excel");
                    movimientos.save(movimiento);
                }
                variantesNuevas++;
                unidades += cantidad;
            }
        }

        return new ImportacionResponse(confirmar, lectura.modelos().size(), productosNuevos,
                variantesNuevas, variantesOmitidas, unidades, advertencias);
    }

    // La marca se toma del nombre del modelo; si no aparece, "Sin marca"
    private String detectarMarca(String nombre) {
        String texto = nombre.toLowerCase(Locale.ROOT);
        if (texto.contains("nike")) {
            return "Nike";
        }
        if (texto.contains("puma")) {
            return "Puma";
        }
        if (texto.contains("adidas")) {
            return "Adidas";
        }
        if (texto.contains("kappa")) {
            return "Kappa";
        }
        return "Sin marca";
    }

    // Devuelve la marca existente; si no existe, la crea solo al confirmar (en la vista previa devuelve null)
    private Marca buscarOCrearMarca(String nombre, boolean crear) {
        return marcas.findByNombreIgnoreCase(nombre).orElseGet(() -> {
            if (!crear) {
                return null;
            }
            Marca nueva = new Marca();
            nueva.setNombre(nombre);
            return marcas.save(nueva);
        });
    }

    private Categoria buscarOCrearCategoria(String nombre, boolean crear) {
        return categorias.findByNombreIgnoreCase(nombre).orElseGet(() -> {
            if (!crear) {
                return null;
            }
            Categoria nueva = new Categoria();
            nueva.setNombre(nombre);
            return categorias.save(nueva);
        });
    }

    private ResponseStatusException invalido(String mensaje) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensaje);
    }
}