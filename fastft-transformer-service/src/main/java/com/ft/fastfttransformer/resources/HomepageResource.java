package com.ft.fastfttransformer.resources;

import com.ft.fastfttransformer.viewer.ContentModelViewerView;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 * HomepageResource
 *
 * @author Simon.Gibbs
 */
@Path("/")
public class HomepageResource {

    @GET
    public ContentModelViewerView getViewer() {
        return new ContentModelViewerView("Fast FT","/content/{{ID}}");
    }

}
