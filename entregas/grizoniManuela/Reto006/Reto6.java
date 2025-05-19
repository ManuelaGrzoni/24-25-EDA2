package Reto006;


import java.util.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

class Persona {
    private String nombre;
    private String carnet;
    private double notaParcial;
    private double notaFinal;
    private double notaContinua;

    public Persona(String nombre, String carnet,
                   double notaParcial, double notaFinal, double notaContinua) {
        this.nombre = nombre;
        this.carnet = carnet;
        this.notaParcial = notaParcial;
        this.notaFinal = notaFinal;
        this.notaContinua = notaContinua;
    }

    public String getNombre() { return nombre; }
    public String getCarnet() { return carnet; }
    public double calcularPromedio() {
        return (notaParcial + notaFinal + notaContinua) / 3.0;
    }

    public void setNotaFinal(double notaFinal) {
        this.notaFinal = notaFinal;
    }
}

class Profesor {
    private String nombre;
    private String departamento;

    public Profesor(String nombre, String departamento) {
        this.nombre = nombre;
        this.departamento = departamento;
    }

    public String getNombre() { return nombre; }
    public String getDepartamento() { return departamento; }
}

class Asignatura {
    private String nombre;
    private String codigo;
    private String periodo;

    public Asignatura(String nombre, String codigo, String periodo) {
        this.nombre = nombre;
        this.codigo = codigo;
        this.periodo = periodo;
    }

    public String getNombre() { return nombre; }
    public String getCodigo() { return codigo; }
    public String getPeriodo() { return periodo; }
}

class Acta {
    private Asignatura asignatura;
    private Profesor profesor;
    private Map<String, Double> calificaciones;
    private String codigoVerificacion;
    private boolean generada = false;

    public Acta(Asignatura asignatura, Profesor profesor) {
        this.asignatura = asignatura;
        this.profesor = profesor;
    }

    public void generar(Persona[] alumnos) {
        if (generada) {
            throw new IllegalStateException("El acta ya fue generada.");
        }
   
        Map<String, Double> temp = new HashMap<>();
        for (Persona p : alumnos) {
            temp.put(p.getCarnet(), p.calcularPromedio());
        }
        this.calificaciones = Collections.unmodifiableMap(temp);
        this.codigoVerificacion = hashSHA256(concatNotas(temp));
        generada = true;
        System.out.println("✅ Acta generada. Código verificación: " + codigoVerificacion);
    }

    private String concatNotas(Map<String, Double> map) {
        StringBuilder sb = new StringBuilder();
        map.entrySet().stream()
           .sorted(Map.Entry.comparingByKey())
           .forEach(e -> sb.append(e.getKey())
                           .append(":")
                           .append(e.getValue())
                           .append(";"));
        return sb.toString();
    }

    private String hashSHA256(String texto) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(texto.getBytes());
            StringBuilder hex = new StringBuilder();
            for (byte b : digest) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }


    public boolean verificarIntegridad() {
        String actual = hashSHA256(concatNotas(this.calificaciones));
        boolean ok = actual.equals(this.codigoVerificacion);
        System.out.println(ok
            ? "🔒 Integridad OK."
            : "⚠️ Integridad FALLIDA!");
        return ok;
    }


    public Map<String, Double> getCalificaciones() {
        return calificaciones;
    }

    public String getCodigoVerificacion() {
        return codigoVerificacion;
    }
}

public class Reto6 {
    public static void main(String[] args) {
        Persona[] alumnos = {
            new Persona("Ana",    "C001", 8.0, 9.0, 10.0),
            new Persona("Luis",   "C002", 7.0, 8.0,  7.5),
            new Persona("Sara",   "C003", 6.5, 7.5,  8.0),
            new Persona("Carlos", "C004", 9.0, 9.0,  9.0),
            new Persona("Elena",  "C005",10.0, 9.5,  9.0)
        };

        Asignatura asig = new Asignatura("EDA 2024-25", "EDA101", "Ordinaria");
        Profesor prof   = new Profesor("Dr. García", "Informática");

        Acta acta = new Acta(asig, prof);
        acta.generar(alumnos);

        try {
            acta.getCalificaciones().put("C002", 10.0);
        } catch (UnsupportedOperationException e) {
            System.out.println("❌ No se puede cambiar la calificación almacenada.");
        }

        alumnos[1].setNotaFinal(10.0);

        acta.verificarIntegridad();
    }
}

