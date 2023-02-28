package com.distribuida.rest;

import com.distribuida.clientes.authors.AuthorRestProxy;
import com.distribuida.clientes.authors.AuthorsCliente;
import com.distribuida.db.Book;
import com.distribuida.dtos.BookDto;
import com.distribuida.servicios.BookRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@ApplicationScoped
@Path("/books")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BookRest {

    @Inject BookRepository bookService;

    @RestClient
    @Inject AuthorRestProxy proxyAuthor;

    @GET
    @Path("/{id}")
    public Response findById(@PathParam("id") Integer id) {
        Optional<Book> ret = bookService.findById(id);

        if( ret.isPresent() ) {
            return Response.ok(ret.get()).build();
        }
        else {
            String msg = String.format( "Book[id=%d] not found.", id );

            return Response.status(Response.Status.NOT_FOUND)
                    .entity(msg)
                    .build();
        }
    }

    @GET
    public List<Book> findAll() {
        System.out.println("Buscando todos");
        return bookService.findAll();
    }

    @GET
    @Path("/all")
    @Retry(delay = 1000)
    @Fallback(fallbackMethod = "findAllFallback")
    public List<BookDto> findAllCompleto() {
        var books = bookService.findAll();

        return books.stream()
                .map(s -> {
                    System.out.println("********* buscando " + s.getId() );

                    AuthorsCliente author = proxyAuthor.findById(s.getId().longValue());
                    return new BookDto(
                            s.getId(),
                            s.getIsbn(),
                            s.getTitle(),
                            s.getAuthor(),
                            s.getPrice(),
                            String.format("%s, %s", author.getLastName(), author.getFirtName())
                    );
                })
                .collect(Collectors.toList());
    }

    @POST
    public void insert(Book book) {
        bookService.insert(book);
    }

    @PUT
    @Path("/{id}")
    public void update(Book book, @PathParam("id") Integer id) {
        book.setId(id);

        bookService.update(book);
    }

    @DELETE
    @Path("/{id}")
    public void delete( @PathParam("id") Integer id ) {
        bookService.delete(id);
    }

    public List<BookDto> findAllFallback() {
        return Collections.emptyList();
    }
}
