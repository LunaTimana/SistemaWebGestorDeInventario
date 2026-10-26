package com.emsstore.backend.service;

import com.emsstore.backend.dto.HistorialItemResponse;
import com.emsstore.backend.dto.ProductoDetalleResponse;
import com.emsstore.backend.dto.ProductoRequest;
import com.emsstore.backend.model.Categoria;
import com.emsstore.backend.model.EstadoUsuario;
import com.emsstore.backend.model.Marca;
import com.emsstore.backend.model.MotivoMovimiento;
import com.emsstore.backend.model.MovimientoInventario;
import com.emsstore.backend.model.Producto;
import com.emsstore.backend.model.Proveedor;
import com.emsstore.backend.model.TipoMovimiento;
import com.emsstore.backend.model.Usuario;
import com.emsstore.backend.model.VarianteProducto;
import com.emsstore.backend.repository.CategoriaRepository;
import com.emsstore.backend.repository.MarcaRepository;
import com.emsstore.backend.repository.MovimientoInventarioRepository;
import com.emsstore.backend.repository.ProductoRepository;
import com.emsstore.backend.repository.ProveedorRepository;
import com.emsstore.backend.repository.UsuarioRepository;
import com.emsstore.backend.repository.VarianteProductoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ProductoService {

    private final ProductoRepository productos;
    private final VarianteProductoRepository variantes;
    private final MovimientoInventarioRepository movimientos;
    private final MarcaRepository marcas;
    private final CategoriaRepository categorias;
    private final ProveedorRepository proveedores;
    private final UsuarioRepository usuarios;

    public ProductoService(ProductoRepository productos,
                           VarianteProductoRepository variantes,
                           MovimientoInventarioRepository movimientos,
                           MarcaRepository marcas,
                           CategoriaRepository categorias,
                           ProveedorRepository proveedores,
                           UsuarioRepository usuarios) {
        this.productos = productos;
        this.variantes = variantes;
        this.movimientos = movimientos;
        this.marcas = marcas;
        this.categorias = categorias;
        this.proveedores = proveedores;
        this.usuarios = usuarios;
    }

    // Todo ocurre en una transacción: si algo falla, no se guarda nada
    @Transactional
    public ProductoDetalleResponse registrar(ProductoRequest solicitud) {
        Marca marca = marcas.findById(solicitud.marcaId())
                .orElseThrow(() -> invalido("La marca no existe"));
        Categoria categoria = categorias.findById(solicitud.categoriaId())
                .orElseThrow(() -> invalido("La categoría no existe"));

        Proveedor proveedor = null;
        if (solicitud.proveedorId() != null) {
            proveedor = proveedores.findById(solicitud.proveedorId())
                    .orElseThrow(() -> invalido("El proveedor no existe"));
        }

        int cantidad = solicitud.cantidadInicial();
        Usuario responsable = null;
        if (cantidad > 0) {
            if (solicitud.responsableId() == null) {
                throw invalido("Indica el responsable del ingreso inicial");
            }
            responsable = usuarios.findById(solicitud.responsableId())
                    .filter(u -> u.getEstado() == EstadoUsuario.activo)
                    .orElseThrow(() -> invalido("El responsable no existe o está inactivo"));
        }

        String nombre = solicitud.nombre().trim();
        String talla = solicitud.talla().trim();
        String color = solicitud.color().trim();

        String sku = SkuGenerator.generar(marca.getNombre(), nombre, talla, color);
        if (variantes.existsByCodigoUnico(sku)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya existe una variante con el código " + sku);
        }

        // Si ya existe ese modelo de esa marca, se le agrega la variante y se conservan
        // su costo y su precio; si no, se crea el producto nuevo.
        Producto producto = productos
                .findFirstByMarcaIdAndNombreModeloIgnoreCase(marca.getId(), nombre)
                .orElse(null);
        if (producto == null) {
            producto = new Producto();
            producto.setNombreModelo(nombre);
            producto.setMarca(marca);
            producto.setCategoria(categoria);
            producto.setProveedor(proveedor);
            producto.setCostoUnitario(solicitud.costoUnitario());
            producto.setPrecioVenta(solicitud.precioVenta());
            producto.setImagenUrl(solicitud.imagenUrl());
            producto = productos.save(producto);
        }

        VarianteProducto variante = new VarianteProducto();
        variante.setProducto(producto);
        variante.setTalla(talla);
        variante.setColor(color);
        variante.setCodigoUnico(sku);
        variante.setStockActual(cantidad);
        variante.setStockMinimo(solicitud.stockMinimo() == null ? 0 : solicitud.stockMinimo());
        variante = variantes.save(variante);

        // El stock inicial queda registrado como un movimiento de entrada
        List<HistorialItemResponse> historial = List.of();
        if (cantidad > 0) {
            MovimientoInventario movimiento = new MovimientoInventario();
            movimiento.setVariante(variante);
            movimiento.setTipo(TipoMovimiento.entrada);
            movimiento.setMotivo(MotivoMovimiento.compra);
            movimiento.setCantidad(cantidad);
            movimiento.setUsuario(responsable);
            movimiento.setProveedor(proveedor != null ? proveedor : producto.getProveedor());
            movimiento.setComentario("Ingreso inicial");
            movimiento = movimientos.save(movimiento);
            historial = List.of(HistorialItemResponse.desde(movimiento));
        }

        return ProductoDetalleResponse.desde(variante, historial);
    }

    private ResponseStatusException invalido(String mensaje) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensaje);
    }
}