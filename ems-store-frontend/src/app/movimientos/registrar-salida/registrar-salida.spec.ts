import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RegistrarSalida } from './registrar-salida';

describe('RegistrarSalida', () => {
  let component: RegistrarSalida;
  let fixture: ComponentFixture<RegistrarSalida>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RegistrarSalida],
    }).compileComponents();

    fixture = TestBed.createComponent(RegistrarSalida);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
