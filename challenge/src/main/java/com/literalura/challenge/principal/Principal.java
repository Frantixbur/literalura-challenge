package com.literalura.challenge.principal;

import com.literalura.challenge.back.BackAPI;
import com.literalura.challenge.back.TransformaDatos;
import com.literalura.challenge.database.Data;
import com.literalura.challenge.database.DataAuthor;
import com.literalura.challenge.database.DataBook;

import java.util.*;

public class Principal {
    private static final String URL_BASE = "https://gutendex.com/books/";
    private BackAPI consumoAPI = new BackAPI();
    private TransformaDatos conversor = new TransformaDatos();
    private Scanner teclado = new Scanner(System.in);

    public void muestraMenu(){
        var json = consumoAPI.catchData(URL_BASE);
        System.out.println(json);
        var datos = conversor.catchData(json, Data.class);
        System.out.println(datos);

        while (true) {
            mostrarOpcionesMenu();
            int option = obtenerOpcion();

            if (option == 0) {
                System.out.println("Finalizo el buscador de libros.");
                break;
            }

            switch (option) {
                case 1 -> buscarLibroPorTitulo();
                case 2 -> listarLibrosRegistrados();
                case 3 -> listarAutoresRegistrados();
                case 4 -> listarAutoresVivosPorAnio();
                case 5 -> listarLibrosPorIdioma();
                default -> System.out.println();
            }
        }
    }

    private void mostrarOpcionesMenu() {
        System.out.println(" ");
        System.out.println("****************************************");
        System.out.println("Sea bienvenido/a al buscador de libros:");
        System.out.println();
        System.out.println("------------  Menú  ------------");
        System.out.println("1. Buscar libro por título");
        System.out.println("2. Listar libros registrados");
        System.out.println("3. Listar autores registrados");
        System.out.println("4. Listar autores vivos en un determinado año");
        System.out.println("5. Listar libros por idioma");
        System.out.println("0. Salir");
        System.out.println("Seleccione una opción (1-5) o 0 par salir: ");
        System.out.println("****************************************");
    }

    private int obtenerOpcion() {
        int option = -1;
        try {
            option = teclado.nextInt();
            teclado.nextLine(); // Limpiar el buffer
        } catch (InputMismatchException e) {
            System.out.println("Entrada no válida. Por favor, seleccione una opción (1-5) o 0 par salir.");
            teclado.next(); // Limpiar la entrada inválida
        }
        return option;
    }

    private void buscarLibroPorTitulo() {
        System.out.println("Ingrese el nombre del libro que desea buscar");
        var tituloLibro = teclado.nextLine();
        if (tituloLibro.isEmpty()) {
            System.out.println("No ingresó un título. Inténtelo de nuevo.");
            return;
        }
        var json = consumoAPI.catchData(URL_BASE + "?search=" + tituloLibro.replace(" ","+"));
        var datosBusqueda = conversor.catchData(json, Data.class);
        Optional<DataBook> libroBuscado = datosBusqueda.resultados().stream()
                .filter(l -> l.titulo().toUpperCase().contains(tituloLibro.toUpperCase()))
                .findFirst();

        if(libroBuscado.isPresent()){
            mostrarInformacionLibro(libroBuscado.get());
        } else {
            System.out.println("Libro no encontrado");
        }
    }

    private void listarLibrosRegistrados() {
        System.out.println("Lista de libros registrados: ");
        var json = consumoAPI.catchData(URL_BASE + "?search=");
        var datosBusqueda = conversor.catchData(json, Data.class);
        List<DataBook> librosRegistrados = datosBusqueda.resultados();

        for (DataBook libro : librosRegistrados) {
            mostrarInformacionLibro(libro);
        }
    }

    private void listarAutoresRegistrados() {
        System.out.println("Lista de autores registrados: ");
        var json = consumoAPI.catchData(URL_BASE + "?search=");
        var datosBusqueda = conversor.catchData(json, Data.class);
        List<DataBook> librosRegistrados = datosBusqueda.resultados();

        Map<DataAuthor, List<String>> autoresLibrosMap = new HashMap<>();
        for (DataBook libro : librosRegistrados) {
            for (DataAuthor autor : libro.autor()) {
                autoresLibrosMap.computeIfAbsent(autor, k -> new ArrayList<>()).add(libro.titulo());
            }
        }

        for (Map.Entry<DataAuthor, List<String>> entry : autoresLibrosMap.entrySet()) {
            mostrarInformacionAutor(entry.getKey(), entry.getValue());
        }
    }

    private void listarAutoresVivosPorAnio() {
        System.out.println("Ingrese el año vivo de autores(es) que desea buscar:");
        int anioBusqueda = teclado.nextInt();
        teclado.nextLine(); // Limpiar el buffer

        var json = consumoAPI.catchData(URL_BASE + "?search=");
        var datosBusqueda = conversor.catchData(json, Data.class);
        List<DataBook> librosRegistrados = datosBusqueda.resultados();

        Map<DataAuthor, List<String>> autoresVivosLibrosMap = new HashMap<>();
        for (DataBook libro : librosRegistrados) {
            for (DataAuthor autor : libro.autor()) {
                int nacimiento = Integer.parseInt(autor.fechaDeNacimiento());
                int fallecimiento = autor.fechaDeFallecimiento().isEmpty() ? Integer.MAX_VALUE : Integer.parseInt(autor.fechaDeFallecimiento());

                if (nacimiento <= anioBusqueda && fallecimiento >= anioBusqueda) {
                    autoresVivosLibrosMap.computeIfAbsent(autor, k -> new ArrayList<>()).add(libro.titulo());
                }
            }
        }

        if (autoresVivosLibrosMap.isEmpty()) {
            System.out.println("No se encontraron autores vivos en el año " + anioBusqueda + ".");
        } else {
            System.out.println("Lista de autores vivos en el año " + anioBusqueda + ":");
            for (Map.Entry<DataAuthor, List<String>> entry : autoresVivosLibrosMap.entrySet()) {
                mostrarInformacionAutor(entry.getKey(), entry.getValue());
            }
        }
    }

    private void listarLibrosPorIdioma() {
        System.out.println("Ingrese el idioma que deseas buscar: ");
        System.out.println();
        System.out.println("1) es - español");
        System.out.println("2) en - ingles");
        System.out.println("3) fr - frances");
        System.out.println("4) pt - portugues");
        int idiomaOpcion = teclado.nextInt();
        teclado.nextLine(); // Limpiar el buffer

        String codigoIdioma;
        switch (idiomaOpcion) {
            case 1 -> codigoIdioma = "es";
            case 2 -> codigoIdioma = "en";
            case 3 -> codigoIdioma = "fr";
            case 4 -> codigoIdioma = "pt";
            default -> {
                System.out.println("Código de idioma no válido. Inténtelo de nuevo.");
                return;
            }
        }

        var json = consumoAPI.catchData(URL_BASE + "?languages=" + codigoIdioma);
        var datosBusqueda = conversor.catchData(json, Data.class);
        List<DataBook> librosRegistrados = datosBusqueda.resultados();

        System.out.println("Lista de libros en el idioma " + codigoIdioma + ":");
        for (DataBook libro : librosRegistrados) {
            mostrarInformacionLibro(libro);
        }
    }

    private void mostrarInformacionLibro(DataBook libro) {
        System.out.println(" ");
        System.out.println("---------- LIBRO ----------");
        System.out.println("Título: " + libro.titulo());

        for (DataAuthor autor : libro.autor()) {
            System.out.println("Autor: " + autor.nombre());
            System.out.println("Fecha de nacimiento: "+ autor.fechaDeNacimiento());
        }

        System.out.println("Idioma: " + libro.idiomas());
        System.out.println("Número de descargas: " + libro.numeroDeDescargas());
        System.out.println("---------------------------");
    }

    private void mostrarInformacionAutor(DataAuthor autor, List<String> libros) {
        System.out.println();
        System.out.println("---------- AUTOR ----------");
        System.out.println("Nombre: " + autor.nombre());
        System.out.println("Fecha de nacimiento: " + autor.fechaDeNacimiento());
        System.out.println("Fecha de fallecimiento: " + (autor.fechaDeFallecimiento().isEmpty() ? "N/A" : autor.fechaDeFallecimiento()));
        System.out.println("Libros: " + String.join(", ", libros));
        System.out.println("---------------------------");
    }
}