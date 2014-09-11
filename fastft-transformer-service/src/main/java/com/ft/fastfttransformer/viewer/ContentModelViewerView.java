package com.ft.fastfttransformer.viewer;

import com.google.common.base.Charsets;
import io.dropwizard.views.View;

/**
 * A view class that causes the rendering of a client side Content Model Viewer application.
 *
 * @author Simon.Gibbs
 */
public class ContentModelViewerView extends View {


    private String appName;
    private String modelUriTemplate;

    public ContentModelViewerView(String appName, String modelUriTemplate) {
        super("contentModelViewer.ftl", Charsets.UTF_8);
        this.appName = appName;
        this.modelUriTemplate = modelUriTemplate;
    }

    /* used in the free marker */

    @SuppressWarnings("unused")
    public String getAppName() {
        return appName;
    }

    @SuppressWarnings("unused")
    public String getModelUriTemplate() {
        return modelUriTemplate;
    }
}
