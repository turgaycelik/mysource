package com.atlassian.jira.dev.functest.ao;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.google.common.collect.Lists;
import net.java.ao.DBParam;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * @since v4.4
 */
@Path ("blog")
@Consumes (MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class BlogResource
{
    private final ActiveObjects ao;

    public BlogResource(ActiveObjects ao)
    {
        this.ao = ao;
    }

    @GET
    @Path("{id}")
    public Response getBlog(@PathParam("id") long id)
    {
        BlogAO blogAO = ao.get(BlogAO.class, id);

        if (blogAO == null)
        {
            return Response.status(Response.Status.NOT_FOUND).cacheControl(never()).build();
        }
        else
        {
            return Response.ok(new BlogBean(blogAO)).cacheControl(never()).build();
        }
    }

    @POST
    @Consumes (MediaType.APPLICATION_JSON)
    public Response createBlog(BlogBean bean, @Context UriInfo builder)
    {
        BlogAO blogAO = ao.create(BlogAO.class, new DBParam("AUTHOR", bean.author), new DBParam("TEXT", bean.text));

        UriBuilder path = builder.getBaseUriBuilder().path(BlogResource.class)
                .path(String.valueOf(blogAO.getID()));

        return Response.created(path.build()).cacheControl(never()).build();
    }

    @PUT
    @Consumes (MediaType.APPLICATION_JSON)
    public Response editBlog(BlogBean bean, @Context UriInfo builder)
    {
        BlogAO blogAO = ao.get(BlogAO.class, bean.id);
        blogAO.setAuthor(bean.author);
        blogAO.setText(bean.text);
        blogAO.save();

        UriBuilder path = builder.getBaseUriBuilder().path(BlogResource.class)
                .path(String.valueOf(blogAO.getID()));

        return Response.ok(path.build()).cacheControl(never()).build();
    }

    @GET
    @Path("{id}/comments")
    public Response getComments(@PathParam("id") long id)
    {
        BlogAO blogAO = ao.get(BlogAO.class, id);

        if (blogAO == null)
        {
            return Response.status(Response.Status.NOT_FOUND).cacheControl(never()).build();
        }
        else
        {
            return Response.ok(CommentBean.asBeans(blogAO.getComments())).cacheControl(never()).build();
        }
    }
    
    @GET
    public Response getAll()
    {
        return Response.ok(BlogBean.asBeans(ao.find(BlogAO.class))).cacheControl(never()).build();
    }

    @DELETE
    public Response deleteAll()
    {
        ao.delete(ao.find(BlogAO.class));
        return Response.status(Response.Status.NO_CONTENT).cacheControl(never()).build();
    }

    @DELETE
    @Path("{id}")
    public Response deleteBlog(@PathParam("id") long id)
    {
        BlogAO blogAO = ao.get(BlogAO.class, id);

        if (blogAO == null)
        {
            return Response.status(Response.Status.NOT_FOUND).cacheControl(never()).build();
        }
        else
        {
            ao.delete(blogAO);
            return Response.noContent().cacheControl(never()).build();
        }
    }
    private static javax.ws.rs.core.CacheControl never()
    {
        javax.ws.rs.core.CacheControl cacheNever = new javax.ws.rs.core.CacheControl();
        cacheNever.setNoStore(true);
        cacheNever.setNoCache(true);

        return cacheNever;
    }

    @XmlRootElement (name = "blog")
    public static class BlogBean
    {
        @XmlElement
        private String author;

        @XmlElement
        private String text;

        @XmlElement
        private List<CommentBean> comments;

        @XmlElement
        private Long id;

        public BlogBean()
        {
        }

        public BlogBean(BlogAO blogAO)
        {
            this.id = blogAO.getID();
            this.author = blogAO.getAuthor();
            this.text = blogAO.getText();
            this.comments = CommentBean.asBeans(blogAO.getComments());
        }

        public static List<BlogBean> asBeans(BlogAO[] blogAOs)
        {
            List<BlogBean> beans = Lists.newArrayList();
            for (BlogAO blogAO : blogAOs)
            {
                beans.add(new BlogBean(blogAO));
            }
            return beans;
        }
    }

    @XmlRootElement (name = "comment")
    public static class CommentBean
    {
        @XmlElement
        private String author;

        @XmlElement
        private String comment;

        @XmlElement
        private long id;

        public CommentBean()
        {
        }

        public CommentBean(CommentAO commentAO)
        {
            this.author = commentAO.getAuthor();
            this.comment = commentAO.getComment();
            this.id = commentAO.getID();
        }

        public static List<CommentBean> asBeans(CommentAO[] commentAOs)
        {
            List<CommentBean> beans = Lists.newArrayList();
            for (CommentAO commentAO : commentAOs)
            {
                beans.add(new CommentBean(commentAO));
            }
            return beans;
        }
    }
}
