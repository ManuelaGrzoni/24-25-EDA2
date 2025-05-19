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
    private List<Persona> alumnos = new ArrayList<>();
    private Map<String, Double> calificaciones;
    private String codigoVerificacion;
    private boolean generada = false;

    public Acta(Asignatura asignatura, Profesor profesor) {
        this.asignatura = asignatura;
        this.profesor = profesor;
    }

    public void generar(Persona[] alumnosArray) {
        if (generada) {
            throw new IllegalStateException("El acta ya fue generada.");
        }
        this.alumnos = Arrays.asList(alumnosArray);

        Map<String, Double> temp = new HashMap<>();
        for (Persona p : this.alumnos) {
            temp.put(p.getCarnet(), p.calcularPromedio());
        }
        this.calificaciones = Collections.unmodifiableMap(temp);
        this.codigoVerificacion = hashSHA256(concatNotas(temp));
        generada = true;
    }

    private String concatNotas(Map<String, Double> map) {
        StringBuilder sb = new StringBuilder();
        map.entrySet().stream()
           .sorted(Map.Entry.comparingByKey())
           .forEach(e -> sb.append(e.getKey())
                           .append(":")
                           .append(String.format(Locale.ROOT, "%.2f", e.getValue()))
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
        return actual.equals(this.codigoVerificacion);
    }

    public void imprimirActa() {
        System.out.println();
        System.out.println("════════════════════════════════════════════════════");
        System.out.printf(" Acta de %s (%s) - %s%n",
            asignatura.getNombre(),
            asignatura.getCodigo(),
            asignatura.getPeriodo());

        System.out.printf(" Profesor: %s [%s]%n",
            profesor.getNombre(),
            profesor.getDepartamento());

        System.out.println("════════════════════════════════════════════════════");
        System.out.printf("%-8s │ %-20s │ %7s%n",
            "Carnet", "Nombre", "Promedio");

        System.out.println("─────────┼──────────────────────┼─────────");
        for (Persona p : alumnos) {
            double prom = calificaciones.get(p.getCarnet());
            System.out.printf("%-8s │ %-20s │ %7.2f%n",
                p.getCarnet(),
                p.getNombre(),
                prom);
        }
        System.out.println("════════════════════════════════════════════════════");
        System.out.printf(" Código verificación: %s%n", codigoVerificacion);
        System.out.println("════════════════════════════════════════════════════");
    }

    public Map<String, Double> getCalificaciones() {
        return calificaciones;
    }
    public String getCodigoVerificacion() {
        return codigoVerificacion;
    }
}

public class Reto6{
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
        acta.imprimirActa();

        try {
            acta.getCalificaciones().put("C002", 10.0);
        } catch (UnsupportedOperationException e) {
            System.out.println("No se puede cambiar la calificación almacenada");
        }

        alumnos[1].setNotaFinal(10.0);

        boolean ok = acta.verificarIntegridad();
        System.out.println(ok
            ? "Integridad OK."
            : "Integridad FALLIDA!");
    }
}