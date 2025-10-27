package com.vmacgar473.aad;

import java.io.IOException;
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

    public static void main(String[] args) {
        SpringApplication.run(AadApplication.class, args);
    }

    @Override
    public void run(String... args) {
        System.out.println("=== Mini Explorador de Ficheros ===");
        Path dir = solicitarDirectorio();
        if (dir == null) {
            // Ya se mostró el error correspondiente.
            return;
        }

        while (true) {
            System.out.println();
            listarContenidoDirectorio(dir);
            System.out.println();
            mostrarMenu();
            int opcion = leerOpcion();

            switch (opcion) {
                case 1 -> dir = cambiarDirectorio(dir);
                case 2 -> crearFichero(dir);
                case 3 -> moverFichero(dir);
                case 4 -> borrarFichero(dir);
                case 5 -> {
                    System.out.println("Saliendo... ¡Hasta luego!");
                    return;
                }
                default -> System.out.println("Opción no válida. Inténtalo de nuevo.");
            }
        }
    }

    // ====== Operaciones ======

    private Path solicitarDirectorio() {
        System.out.print("Introduce la ruta de un directorio: ");
        String entrada = scanner.nextLine().trim();

        Path dir = entrada.isEmpty() ? Paths.get(".").toAbsolutePath().normalize()
                : Paths.get(entrada).toAbsolutePath().normalize();

        if (!Files.exists(dir)) {
            System.err.println("Error: el directorio no existe -> " + dir);
            return null;
        }
        if (!Files.isDirectory(dir)) {
            System.err.println("Error: la ruta no es un directorio -> " + dir);
            return null;
        }
        if (!Files.isReadable(dir)) {
            System.err.println("Error: permisos insuficientes de lectura en -> " + dir);
            return null;
        }

        System.out.println("Trabajando en: " + dir);
        return dir;
    }

    private void listarContenidoDirectorio(Path dir) {
        System.out.println("Contenido de: " + dir);
        System.out.println("------------------------------------------------------------");
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            int count = 0;
            for (Path p : stream) {
                count++;
                try {
                    if (Files.isDirectory(p)) {
                        System.out.printf("[DIR ] %-40s%n", p.getFileName());
                    } else if (Files.isRegularFile(p, LinkOption.NOFOLLOW_LINKS)) {
                        BasicFileAttributes attrs = Files.readAttributes(p, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
                        long size = attrs.size();
                        String fecha = formatoFecha(attrs.lastModifiedTime().toInstant());
                        System.out.printf("[FILE] %-30s  %10d bytes  %s%n",
                                p.getFileName(), size, fecha);
                    } else if (Files.isSymbolicLink(p)) {
                        System.out.printf("[LINK] %-40s%n", p.getFileName());
                    } else {
                        System.out.printf("[OTRO] %-40s%n", p.getFileName());
                    }
                } catch (AccessDeniedException ade) {
                    System.out.printf("[????] %-30s  (Acceso denegado)%n", p.getFileName());
                } catch (IOException ioe) {
                    System.out.printf("[ERR ] %-30s  (No se pudo leer: %s)%n", p.getFileName(), ioe.getMessage());
                }
            }
            if (count == 0) {
                System.out.println("(Directorio vacío)");
            }
        } catch (NoSuchFileException e) {
            System.err.println("El directorio ya no existe: " + dir);
        } catch (AccessDeniedException e) {
            System.err.println("Permisos insuficientes para listar: " + dir);
        } catch (IOException e) {
            System.err.println("Error al listar el directorio: " + e.getMessage());
        }
        System.out.println("------------------------------------------------------------");
    }

    private void mostrarMenu() {
        System.out.println("""
                Menú:
                  1) Cambiar de directorio
                  2) Crear fichero vacío
                  3) Mover fichero
                  4) Borrar fichero
                  5) Salir
                """);
        System.out.print("Elige una opción (1-5): ");
    }

    private int leerOpcion() {
        try {
            String line = scanner.nextLine().trim();
            return Integer.parseInt(line);
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    private Path cambiarDirectorio(Path actual) {
        System.out.println("Directorio actual: " + actual);
        System.out.print("Introduce nueva ruta (relativa o absoluta): ");
        String entrada = scanner.nextLine().trim();
        Path nuevo = entrada.isEmpty() ? actual : actual.resolve(entrada).toAbsolutePath().normalize();

        if (!Files.exists(nuevo)) {
            System.err.println("Error: el directorio no existe -> " + nuevo);
            return actual;
        }
        if (!Files.isDirectory(nuevo)) {
            System.err.println("Error: la ruta no es un directorio -> " + nuevo);
            return actual;
        }
        if (!Files.isReadable(nuevo)) {
            System.err.println("Error: permisos insuficientes de lectura en -> " + nuevo);
            return actual;
        }
        System.out.println("Directorio cambiado a: " + nuevo);
        return nuevo;
    }

    private void crearFichero(Path dirBase) {
        System.out.print("Nombre del nuevo fichero (puede ser ruta relativa o absoluta): ");
        String entrada = scanner.nextLine().trim();
        if (entrada.isEmpty()) {
            System.err.println("Nombre vacío. Operación cancelada.");
            return;
        }
        Path destino = resolverRuta(dirBase, entrada);

        try {
            if (Files.exists(destino)) {
                System.err.println("Error: ya existe un fichero o directorio con ese nombre.");
                return;
            }
            Files.createDirectories(destino.getParent()); // por si se indicó una subcarpeta
            Files.createFile(destino);
            System.out.println("Fichero creado: " + destino);
        } catch (AccessDeniedException e) {
            System.err.println("Permisos insuficientes para crear en: " + destino.getParent());
        } catch (IOException e) {
            System.err.println("No se pudo crear el fichero: " + e.getMessage());
        }
    }

    private void moverFichero(Path dirBase) {
        System.out.print("Ruta del fichero a mover (relativa o absoluta): ");
        String origenStr = scanner.nextLine().trim();
        if (origenStr.isEmpty()) {
            System.err.println("Ruta vacía. Operación cancelada.");
            return;
        }
        Path origen = resolverRuta(dirBase, origenStr);

        if (!Files.exists(origen)) {
            System.err.println("Error: el fichero origen no existe.");
            return;
        }
        if (!Files.isRegularFile(origen)) {
            System.err.println("Error: la ruta origen no es un fichero regular.");
            return;
        }

        System.out.print("Nueva ruta de destino (incluye nombre de fichero): ");
        String destinoStr = scanner.nextLine().trim();
        if (destinoStr.isEmpty()) {
            System.err.println("Ruta de destino vacía. Operación cancelada.");
            return;
        }
        Path destino = resolverRuta(dirBase, destinoStr);

        try {
            Files.createDirectories(destino.getParent());
            // No sobrescribimos si ya existe para evitar perder datos
            if (Files.exists(destino)) {
                System.err.println("Error: ya existe un fichero en el destino. Elige otro nombre/ruta.");
                return;
            }
            Files.move(origen, destino, StandardCopyOption.ATOMIC_MOVE);
            System.out.println("Fichero movido:\n  De: " + origen + "\n  A : " + destino);
        } catch (AtomicMoveNotSupportedException e) {
            // Si no se soporta movimiento atómico, hacemos un move normal
            try {
                Files.move(origen, destino);
                System.out.println("Fichero movido (no atómico):\n  De: " + origen + "\n  A : " + destino);
            } catch (AccessDeniedException ade) {
                System.err.println("Permisos insuficientes para mover. Origen/Destino protegidos.");
            } catch (IOException ioe) {
                System.err.println("No se pudo mover el fichero: " + ioe.getMessage());
            }
        } catch (AccessDeniedException e) {
            System.err.println("Permisos insuficientes para mover. Origen/Destino protegidos.");
        } catch (IOException e) {
            System.err.println("No se pudo mover el fichero: " + e.getMessage());
        }
    }

    private void borrarFichero(Path dirBase) {
        System.out.print("Ruta del fichero a borrar (relativa o absoluta): ");
        String entrada = scanner.nextLine().trim();
        if (entrada.isEmpty()) {
            System.err.println("Ruta vacía. Operación cancelada.");
            return;
        }
        Path objetivo = resolverRuta(dirBase, entrada);

        try {
            if (!Files.exists(objetivo)) {
                System.err.println("Error: el fichero no existe.");
                return;
            }
            if (!Files.isRegularFile(objetivo)) {
                System.err.println("Error: solo se permite borrar ficheros regulares (no directorios).");
                return;
            }
            Files.delete(objetivo);
            System.out.println("Fichero borrado: " + objetivo);
        } catch (DirectoryNotEmptyException e) {
            System.err.println("No es un fichero, es un directorio con contenido.");
        } catch (AccessDeniedException e) {
            System.err.println("Permisos insuficientes para borrar: " + objetivo);
        } catch (IOException e) {
            System.err.println("No se pudo borrar el fichero: " + e.getMessage());
        }
    }

    // ====== Utilidades ======

    private Path resolverRuta(Path base, String entrada) {
        Path p = Paths.get(entrada);
        return p.isAbsolute() ? p.normalize() : base.resolve(p).toAbsolutePath().normalize();
    }

    private String formatoFecha(Instant instant) {
        LocalDateTime ldt = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        return DATE_FMT.format(ldt);
    }
}


