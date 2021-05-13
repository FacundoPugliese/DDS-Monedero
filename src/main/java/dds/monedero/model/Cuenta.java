package dds.monedero.model;

import dds.monedero.exceptions.MaximaCantidadDepositosException;
import dds.monedero.exceptions.MaximoExtraccionDiarioException;
import dds.monedero.exceptions.MontoNegativoException;
import dds.monedero.exceptions.SaldoMenorException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Cuenta {

  private double saldo = 0;
  private List<Movimiento> movimientos = new ArrayList<>();

  public Cuenta() {
    saldo = 0;
  }

  public Cuenta(double montoInicial) {
    saldo = montoInicial;
  }

  public void setMovimientos(List<Movimiento> movimientos) {
    this.movimientos = movimientos;
  }

  public void poner(double cuanto) {
    validarOperacionPoner(cuanto);
    this.setSaldo(this.getSaldo() + cuanto);
    this.movimientos.add(new Movimiento(LocalDate.now(), cuanto, true));
  }

  public void sacar(double cuanto) {
    validarOperacionSacar(cuanto);
    this.setSaldo(this.getSaldo() - cuanto);
    this.movimientos.add(new Movimiento(LocalDate.now(), cuanto, false));
  }


  public double getMontoExtraidoA(LocalDate fecha) {
    return getMovimientos().stream()
        .filter(movimiento -> movimiento.fueExtraido(fecha))
        .mapToDouble(Movimiento::getMonto)
        .sum();
  }

  public List<Movimiento> getMovimientos() {
    return movimientos;
  }

  public double getSaldo() {
    return saldo;
  }

  private void setSaldo(double saldo) {
    this.saldo = saldo;
  }

  private void validarOperacionPoner(double saldo){
    if (saldo <= 0) {
      throw new MontoNegativoException(saldo + ": el monto a ingresar debe ser un valor positivo");
    }

    if (getMovimientos().stream().filter(movimiento -> movimiento.fueDepositado(LocalDate.now())).count() >= 3 ) {
      throw new MaximaCantidadDepositosException("Ya excedio los " + 3 + " depositos diarios");
    }
  }

  private void validarOperacionSacar(double saldo){
    if (saldo <= 0) {
      throw new MontoNegativoException(saldo + ": el monto a ingresar debe ser un valor positivo");
    }
    if (getSaldo() - saldo < 0) {
      throw new SaldoMenorException("No puede sacar mas de " + getSaldo() + " $");
    }
    double montoExtraidoHoy = getMontoExtraidoA(LocalDate.now());
    double limite = 1000 - montoExtraidoHoy;
    if (saldo > limite) {
      throw new MaximoExtraccionDiarioException("No puede extraer mas de $ " + 1000
          + " diarios, límite: " + limite);
    }
  }
}
