package io.quarkus.updates.core.quarkus30;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.openrewrite.*;
import org.openrewrite.internal.StringUtils;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.properties.PropertiesVisitor;
import org.openrewrite.properties.tree.Properties;

import java.util.Arrays;
import java.util.List;

@Value
@EqualsAndHashCode(callSuper = true)
public class AdjustApplicationPropertiesWithJakarta extends Recipe {

    static final List<String> JAKARTA_PACKAGES = Arrays.asList(new String[] {
            "javax.activation",
            "javax.annotation.security",
            "javax.annotation.sql",
            "javax.annotation",
            "javax.batch.api.chunk.listener",
            "javax.batch.api.chunk",
            "javax.batch.api.listener",
            "javax.batch.api.partition",
            "javax.batch.api",
            "javax.batch.operations",
            "javax.batch.runtime.context",
            "javax.batch.runtime",
            "javax.decorator",
            "javax.ejb.embeddable",
            "javax.ejb.spi",
            "javax.ejb",
            "javax.el",
            "javax.enterprise.concurrent",
            "javax.enterprise.context.control",
            "javax.enterprise.context.spi",
            "javax.enterprise.context",
            "javax.enterprise.event",
            "javax.enterprise.inject.literal",
            "javax.enterprise.inject.se",
            "javax.enterprise.inject.spi.configurator",
            "javax.enterprise.inject.spi",
            "javax.enterprise.inject",
            "javax.enterprise.util",
            "javax.faces.annotation",
            "javax.faces.application",
            "javax.faces.bean",
            "javax.faces.component.behavior",
            "javax.faces.component.html",
            "javax.faces.component.search",
            "javax.faces.component.visit",
            "javax.faces.component",
            "javax.faces.context",
            "javax.faces.convert",
            "javax.faces.el",
            "javax.faces.event",
            "javax.faces.flow.builder",
            "javax.faces.flow",
            "javax.faces.lifecycle",
            "javax.faces.model",
            "javax.faces.push",
            "javax.faces.render",
            "javax.faces.validator",
            "javax.faces.view.facelets",
            "javax.faces.view",
            "javax.faces.webapp",
            "javax.faces",
            "javax.inject",
            "javax.interceptor",
            "javax.jms",
            "javax.json.bind.adapter",
            "javax.json.bind.annotation",
            "javax.json.bind.config",
            "javax.json.bind.serializer",
            "javax.json.bind.spi",
            "javax.json.bind",
            "javax.json.spi",
            "javax.json.stream",
            "javax.json",
            "javax.jws.soap",
            "javax.jws",
            "javax.mail.event",
            "javax.mail.internet",
            "javax.mail.search",
            "javax.mail.util",
            "javax.mail",
            "javax.persistence.criteria",
            "javax.persistence.metamodel",
            "javax.persistence.spi",
            "javax.persistence",
            "javax.resource.cci",
            "javax.resource.spi.endpoint",
            "javax.resource.spi.security",
            "javax.resource.spi.work",
            "javax.resource.spi",
            "javax.resource",
            "javax.security.auth.message.callback",
            "javax.security.auth.message.config",
            "javax.security.auth.message.module",
            "javax.security.auth.message",
            "javax.security.enterprise.authentication.mechanism.http",
            "javax.security.enterprise.credential",
            "javax.security.enterprise.identitystore",
            "javax.security.enterprise",
            "javax.security.jacc",
            "javax.servlet.annotation",
            "javax.servlet.descriptor",
            "javax.servlet.http",
            "javax.servlet.jsp.el",
            "javax.servlet.jsp.jstl.core",
            "javax.servlet.jsp.jstl.fmt",
            "javax.servlet.jsp.jstl.sql",
            "javax.servlet.jsp.jstl.tlv",
            "javax.servlet.jsp.jstl",
            "javax.servlet.jsp.resources",
            "javax.servlet.jsp.tagext",
            "javax.servlet.jsp",
            "javax.servlet.resources",
            "javax.servlet",
            "javax.transaction",
            "javax.validation.bootstrap",
            "javax.validation.constraints",
            "javax.validation.constraintvalidation",
            "javax.validation.executable",
            "javax.validation.groups",
            "javax.validation.metadata",
            "javax.validation.spi",
            "javax.validation.valueextraction",
            "javax.validation",
            "javax.websocket.server",
            "javax.websocket",
            "javax.ws.rs.client",
            "javax.ws.rs.container",
            "javax.ws.rs.core",
            "javax.ws.rs.ext",
            "javax.ws.rs.sse",
            "javax.ws.rs",
            "javax.xml.bind.annotation.adapters",
            "javax.xml.bind.annotation",
            "javax.xml.bind.attachment",
            "javax.xml.bind.helpers",
            "javax.xml.bind.util",
            "javax.xml.bind",
            "javax.xml.soap",
            "javax.xml.ws.handler.soap",
            "javax.xml.ws.handler",
            "javax.xml.ws.http",
            "javax.xml.ws.soap",
            "javax.xml.ws.spi.http",
            "javax.xml.ws.spi",
            "javax.xml.ws.wsaddressing",
            "javax.xml.ws"
    });

    @Override
    public String getDisplayName() {
        return "Adjust application.properties for jakarta.* packages";
    }

    @Override
    public String getDescription() {
        return "Adjust application.properties for jakarta.* packages";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getSingleSourceApplicableTest() {
        return new HasSourcePath<>("**/application*.properties");
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new AdjustApplicationPropertiesWithJakartaVisitor<>();
    }

    public class AdjustApplicationPropertiesWithJakartaVisitor<P> extends PropertiesVisitor<P> {
        public AdjustApplicationPropertiesWithJakartaVisitor() {
        }

        @Override
        public Properties visitEntry(Properties.Entry entry, P p) {
            Properties.Value updatedValue = updateValue(entry.getValue());
            if (updatedValue != null) {
                entry = entry.withValue(updatedValue);
            }

            return super.visitEntry(entry, p);
        }

        @Nullable // returns null if value should not change
        private Properties.Value updateValue(Properties.Value value) {
            String oldValue = value.getText();

            if (StringUtils.isNullOrEmpty(oldValue)) {
                return null;
            }

            String newValue = oldValue;
            for (String jakartaPackage : JAKARTA_PACKAGES) {
                newValue = newValue.replace(jakartaPackage + ".", jakartaPackage.replace("javax.", "jakarta.") + ".");
            }

            if (oldValue.equals(newValue)) {
                return null;
            }

            return value.withText(newValue);
        }
    }
}
