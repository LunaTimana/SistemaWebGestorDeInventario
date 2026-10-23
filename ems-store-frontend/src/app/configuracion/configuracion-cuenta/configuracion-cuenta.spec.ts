import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ConfiguracionCuenta } from './configuracion-cuenta';

describe('ConfiguracionCuenta', () => {
  let component: ConfiguracionCuenta;
  let fixture: ComponentFixture<ConfiguracionCuenta>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ConfiguracionCuenta],
    }).compileComponents();

    fixture = TestBed.createComponent(ConfiguracionCuenta);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
