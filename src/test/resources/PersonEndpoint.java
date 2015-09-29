import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.*;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;

import br.com.tdc.model.Person;

/**
 * 
 */
@Stateless
@Path("/people")
public class PersonEndpoint {
  @PersistenceContext(unitName = "tdc2015-persistence-unit")
  private EntityManager em;

  @POST
  @Consumes("application/json")
  public Response create(Person entity) {
    em.persist(entity);
    return Response.created(UriBuilder.fromResource(PersonEndpoint.class).path(String.valueOf(entity.getId())).build()).build();
  }

  @DELETE
  @Path("/{id:[0-9][0-9]*}")
  public Response deleteById(@PathParam("id") Long id) {
    Person entity = em.find(Person.class, id);
    if (entity == null) {
      return Response.status(Status.NOT_FOUND).build();
    }
    em.remove(entity);
    return Response.noContent().build();
  }

  @GET
  @Path("/{id:[0-9][0-9]*}")
  @Produces("application/json")
  public Response findById(@PathParam("id") Long id) {
    TypedQuery<Person> findByIdQuery = em.createQuery("SELECT DISTINCT p FROM Person p WHERE p.id = :entityId ORDER BY p.id", Person.class);
    findByIdQuery.setParameter("entityId", id);
    Person entity;
    try {
      entity = findByIdQuery.getSingleResult();
    } catch (NoResultException nre) {
      entity = null;
    }
    if (entity == null) {
      return Response.status(Status.NOT_FOUND).build();
    }
    return Response.ok(entity).build();
  }

  @GET
  @Produces("application/json")
  public List<Person> listAll(@QueryParam("start") Integer startPosition, @QueryParam("max") Integer maxResult) {
    TypedQuery<Person> findAllQuery = em.createQuery("SELECT DISTINCT p FROM Person p ORDER BY p.id", Person.class);
    if (startPosition != null) {
      findAllQuery.setFirstResult(startPosition);
    }
    if (maxResult != null) {
      findAllQuery.setMaxResults(maxResult);
    }
    final List<Person> results = findAllQuery.getResultList();
    return results;
  }

  @PUT
  @Path("/{id:[0-9][0-9]*}")
  @Consumes("application/json")
  public Response update(@PathParam("id") Long id, Person entity) {
    if (entity == null) {
      return Response.status(Status.BAD_REQUEST).build();
    }
    if (id == null) {
      return Response.status(Status.BAD_REQUEST).build();
    }
    if (!id.equals(entity.getId())) {
      return Response.status(Status.CONFLICT).entity(entity).build();
    }
    if (em.find(Person.class, id) == null) {
      return Response.status(Status.NOT_FOUND).build();
    }
    try {
      entity = em.merge(entity);
    } catch (OptimisticLockException e) {
      return Response.status(Status.CONFLICT).entity(e.getEntity()).build();
    }

    return Response.noContent().build();
  }
}
