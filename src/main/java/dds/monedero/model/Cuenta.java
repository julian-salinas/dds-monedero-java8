package dds.monedero.model;

import dds.monedero.exceptions.MaximaCantidadDepositosException;
import dds.monedero.exceptions.MaximoExtraccionDiarioException;
import dds.monedero.exceptions.MontoNegativoException;
import dds.monedero.exceptions.SaldoMenorException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Cuenta {

  // No es necesario inicializar saldo en 0, ya que el valor se inicializa en el constructor de la clase
  private double saldo;
  private List<Movimiento> movimientos = new ArrayList<>();
  private int limite;

  public Cuenta() {
    saldo = 0;
    limite = 1000;
  }

  public Cuenta(double montoInicial) {
    saldo = montoInicial;
    limite = 1000;
  }

  public void setMovimientos(List<Movimiento> movimientos) {
    this.movimientos = movimientos;
  }

  public void poner(double cuanto) {
    // Agrego funciones de validación que lanzan excepciones en casos no deseados.

    this.validarMontoNegativo(cuanto);

    this.validarCantidadDeDepositos();

    new Movimiento(LocalDate.now(), cuanto, true).agregateA(this);
  }

  public void sacar(double cuanto) {

    this.validarMontoNegativo(cuanto);

    this.validarDineroDisponible(cuanto);

    this.validarMaximoExtraccionDiario(cuanto);

    new Movimiento(LocalDate.now(), cuanto, false).agregateA(this);
  }

  public void agregarMovimiento(LocalDate fecha, double cuanto, boolean esDeposito) {
    Movimiento movimiento = new Movimiento(fecha, cuanto, esDeposito);
    movimientos.add(movimiento);
  }

  public double getMontoExtraidoA(LocalDate fecha) {
    return getMovimientos().stream()
        .filter(movimiento -> !movimiento.isDeposito() && movimiento.getFecha().equals(fecha))
        .mapToDouble(Movimiento::getMonto)
        .sum();
  }

  public List<Movimiento> getMovimientos() {
    return movimientos;
  }

  public double getSaldo() {
    return saldo;
  }

  public void setSaldo(double saldo) {
    this.saldo = saldo;
  }

  public void validarMontoNegativo(double cuanto) {
    if (cuanto <= 0) {
      throw new MontoNegativoException(cuanto + ": el monto a ingresar debe ser un valor positivo");
    }
  }

  public void validarCantidadDeDepositos() {
    if (this.getMovimientos().stream().filter(movimiento -> movimiento.isDeposito()).count() >= 3) {
      throw new MaximaCantidadDepositosException("Ya excedio los " + 3 + " depositos diarios");
    }
  }

  public void validarDineroDisponible(double cantidad) {
    if (this.getSaldo() - cantidad < 0) {
      throw new SaldoMenorException("No puede sacar mas de " + getSaldo() + " $");
    }
  }

  public void validarMaximoExtraccionDiario(double cantidad) {
    double montoExtraidoHoy = getMontoExtraidoA(LocalDate.now());

    // en lugar de colocar manualmente el valor 1000, lo agregamos como un atributo límite en la clase Cuenta
    double limiteDisponible = this.limite - montoExtraidoHoy;


    if (cantidad > limiteDisponible) {
      throw new MaximoExtraccionDiarioException("No puede extraer mas de $ " + 1000
          + " diarios, límite: " + limiteDisponible);
    }
  }
}
