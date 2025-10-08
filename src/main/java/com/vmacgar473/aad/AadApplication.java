package com.vmacgar473.aad;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.InputMismatchException;
import java.util.Scanner;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AadApplication implements CommandLineRunner {

    private final Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8);

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ======== Definición del registro fijo ========
    // Estructura: [int id (4)] [nombre 20 bytes ASCII] [double nota (8)] = 32 bytes
    private static final int BYTES_NOMBRE = 20;
    private static final int TAMANO_REGISTRO = 4 + BYTES_NOMBRE + 8;

    //Fichero de datos
    private static final String FICHERO_DATOS = "alumnos.dat";

    public static void main(String[] args) {
        SpringApplication.run(AadApplication.class, args);
    }

    @Override
    public void run(String... args) {
        crearFicheroSiNoExiste();

        while (true) {
            mostrarMenu();
            int opcion = leerEntero("Elige una opción: ");
            try {
                switch (opcion) {
                    case 1:
                        insertarAlumno();
                        break;
                    case 2:
                        consultarPorPosicion();
                        break;
                    case 3:
                        modificarNota();
                        break;
                    case 4:
                        mostrarInformacion();
                        break;
                    case 0:
                        System.out.println("Saliendo... ¡Hasta luego!");
                        return;
                    default:
                        System.out.println("Opción no válida.");
                        break;
                }
            } catch (IOException e) {
                System.err.println("Error de entrada/salida: " + e.getMessage());
            }
            System.out.println();
        }
    }

    // ======== Operaciones principales ========

    // 1) Insertar nuevo alumno (acceso secuencial)
    private void insertarAlumno() throws IOException {
        int id = leerEntero("Introduce el ID del alumno: ");
        String nombre = leerTextoNoVacio("Introduce el nombre (máx. 20 caracteres): ");
        double nota = leerDecimal("Introduce la nota: ");

        try (RandomAccessFile fichero = new RandomAccessFile(FICHERO_DATOS, "rw")) {
            long fin = fichero.length();
            fichero.seek(fin);
            escribirRegistro(fichero, id, nombre, nota);
            System.out.println("Alumno insertado en posición " + (fin / TAMANO_REGISTRO) + " (base 0).");
        }
    }

    // 2) Consultar alumno por posición (acceso aleatorio)
    private void consultarPorPosicion() throws IOException {
        long total = contarRegistros();
        if (total == 0) {
            System.out.println("No hay registros.");
            return;
        }
        long posicion = leerLongEnRango("Posición a consultar (0 - " + (total - 1) + "): ", 0, total - 1);

        try (RandomAccessFile fichero = new RandomAccessFile(FICHERO_DATOS, "r")) {
            fichero.seek(posicion * TAMANO_REGISTRO);
            Alumno alumno = leerRegistro(fichero);
            System.out.printf("Posición %d -> ID=%d | Nombre=\"%s\" | Nota=%.2f%n",
                    posicion, alumno.id(), alumno.nombre(), alumno.nota());
        }
    }

    // 3) Modificar solo la nota (sin reescribir todo el fichero)
    private void modificarNota() throws IOException {
        long total = contarRegistros();
        if (total == 0) {
            System.out.println("No hay registros.");
            return;
        }
        long posicion = leerLongEnRango("Posición a modificar (0 - " + (total - 1) + "): ", 0, total - 1);
        double nuevaNota = leerDecimal("Nueva nota: ");

        try (RandomAccessFile fichero = new RandomAccessFile(FICHERO_DATOS, "rw")) {
            long base = posicion * TAMANO_REGISTRO;
            long desplazamientoNota = base + 4 + BYTES_NOMBRE; // offset de la nota
            fichero.seek(desplazamientoNota);
            fichero.writeDouble(nuevaNota);
        }
        System.out.println("Nota modificada correctamente.");
    }

    // 4) Mostrar número de registros y tamaño total
    private void mostrarInformacion() throws IOException {
        long total = contarRegistros();
        long bytes = Files.size(Path.of(FICHERO_DATOS));
        System.out.printf("Registros: %d | Tamaño del fichero: %d bytes | Tamaño de registro: %d bytes%n",
                total, bytes, TAMANO_REGISTRO);
    }

    // ======== Métodos auxiliares de lectura/escritura ========

    private void escribirRegistro(RandomAccessFile fichero, int id, String nombre, double nota) throws IOException {
        fichero.writeInt(id);
        byte[] nombreBytes = convertirAFijo(nombre, BYTES_NOMBRE);
        fichero.write(nombreBytes);
        fichero.writeDouble(nota);
    }

    private Alumno leerRegistro(RandomAccessFile fichero) throws IOException {
        int id = fichero.readInt();
        byte[] nombreBytes = new byte[BYTES_NOMBRE];
        int leidos = fichero.read(nombreBytes);
        if (leidos != BYTES_NOMBRE) {
            throw new IOException("Registro corrupto (nombre incompleto).");
        }
        String nombre = convertirDesdeFijo(nombreBytes);
        double nota = fichero.readDouble();
        return new Alumno(id, nombre, nota);
    }

    private long contarRegistros() throws IOException {
        long bytes = Files.size(Path.of(FICHERO_DATOS));
        if (bytes % TAMANO_REGISTRO != 0) {
            throw new IOException("El fichero parece estar dañado (tamaño irregular).");
        }
        return bytes / TAMANO_REGISTRO;
    }

    private static byte[] convertirAFijo(String texto, int tamano) {
        String recortado = texto == null ? "" : texto.trim();
        byte[] crudo = recortado.getBytes(StandardCharsets.US_ASCII);
        byte[] fijo = new byte[tamano];
        int longitud = Math.min(crudo.length, tamano);
        System.arraycopy(crudo, 0, fijo, 0, longitud);
        for (int i = longitud; i < tamano; i++) fijo[i] = ' ';
        return fijo;
    }

    private static String convertirDesdeFijo(byte[] bytes) {
        String texto = new String(bytes, StandardCharsets.US_ASCII);
        return quitarEspaciosDerecha(texto);
    }

    private static String quitarEspaciosDerecha(String texto) {
        int fin = texto.length();
        while (fin > 0 && texto.charAt(fin - 1) == ' ') fin--;
        return texto.substring(0, fin);
    }

    private static void crearFicheroSiNoExiste() {
        try {
            Path ruta = Path.of(FICHERO_DATOS);
            if (!Files.exists(ruta)) {
                Files.createFile(ruta);
            }
        } catch (IOException e) {
            throw new RuntimeException("No se pudo crear el fichero: " + FICHERO_DATOS, e);
        }
    }

    // ======== Métodos de lectura por consola ========

    private void mostrarMenu() {
        System.out.println("""
                ===============================
                GESTIÓN DE ALUMNOS (BINARIO)
                ===============================
                1) Insertar alumno (secuencial)
                2) Consultar alumno por posición (aleatorio)
                3) Modificar nota de un alumno
                4) Mostrar información del fichero
                0) Salir
                """);
    }

    private int leerEntero(String mensaje) {
        while (true) {
            try {
                System.out.print(mensaje);
                String linea = scanner.nextLine().trim();
                return Integer.parseInt(linea);
            } catch (NumberFormatException e) {
                System.out.println("Introduce un número entero válido.");
            }
        }
    }

    private double leerDecimal(String mensaje) {
        while (true) {
            try {
                System.out.print(mensaje);
                String linea = scanner.nextLine().trim();
                return Double.parseDouble(linea.replace(',', '.'));
            } catch (NumberFormatException e) {
                System.out.println("Introduce un número decimal válido.");
            }
        }
    }

    private long leerLongEnRango(String mensaje, long minimo, long maximo) {
        while (true) {
            try {
                System.out.print(mensaje);
                String linea = scanner.nextLine().trim();
                long valor = Long.parseLong(linea);
                if (valor < minimo || valor > maximo) {
                    System.out.printf("El valor debe estar entre %d y %d.%n", minimo, maximo);
                } else {
                    return valor;
                }
            } catch (NumberFormatException e) {
                System.out.println("Introduce un número válido.");
            }
        }
    }

    private String leerTextoNoVacio(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String texto = scanner.nextLine();
            if (texto == null || texto.trim().isEmpty()) {
                System.out.println("El texto no puede estar vacío.");
            } else {
                return texto;
            }
        }
    }

    // ======== Clase interna Alumno ========
    private record Alumno(int id, String nombre, double nota) {}
}



