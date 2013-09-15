package de.beyondjava.jsfComponents.sync;

import java.io.IOException;

import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.context.*;
import javax.faces.event.*;
import javax.faces.render.FacesRenderer;

import org.primefaces.component.api.AjaxSource;
import org.primefaces.context.RequestContext;
import org.primefaces.renderkit.CoreRenderer;
import org.primefaces.util.*;

import de.beyondjava.jsfComponents.common.ELTools;

@FacesRenderer(componentFamily = "org.primefaces.component", rendererType = "de.beyondjava.sync")
public class SyncRenderer extends CoreRenderer {

   @Override
   public void decode(FacesContext context, UIComponent component) {
      Sync command = (Sync) component;

      if (context.getExternalContext().getRequestParameterMap().containsKey(command.getClientId(context))) {
         ActionEvent event = new ActionEvent(command);
         if (command.isImmediate()) {
            event.setPhaseId(PhaseId.APPLY_REQUEST_VALUES);
         }
         else {
            event.setPhaseId(PhaseId.INVOKE_APPLICATION);
         }

         command.queueEvent(event);
      }
   }

   @Override
   public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
      if (true) {
         String attributeName = ELTools.getCoreValueExpression(component);
         if (attributeName.contains(".")) {
            int pos = attributeName.lastIndexOf('.');
            attributeName = attributeName.substring(pos + 1);
         }
         Object value = component.getAttributes().get("value");
         ResponseWriter writer = context.getResponseWriter();
         Sync command = (Sync) component;
         AjaxSource source = command;
         String clientId = command.getClientId(context);
         String name = command.getName();
         if (name == null) {
            name = clientId.replace(":", "_");
         }
         UIComponent form = ComponentUtils.findParentForm(context, command);
         if (form == null) {
            throw new FacesException("Sync '" + name + "'must be inside a form.");
         }

         AjaxRequestBuilder builder = RequestContext.getCurrentInstance().getAjaxRequestBuilder();
         source.getUpdate();
         String request = builder.init().source(clientId).form(form.getClientId(context))
               .update(component, source.getUpdate()).async(source.isAsync()).global(source.isGlobal())
               .process(component, source.getProcess())
               .partialSubmit(source.isPartialSubmit(), command.isPartialSubmitSet())
               .ignoreAutoUpdate(source.isIgnoreAutoUpdate()).onstart(source.getOnstart()).onerror(source.getOnerror())
               .onsuccess(source.getOnsuccess()).oncomplete(source.getOncomplete()).passParams().build();
         // String request =
         // builder.init().source(clientId).form(form.getClientId(context))
         // .process(component, source.getProcess()).update(component,
         // source.getUpdate()).async(source.isAsync())
         // .global(source.isGlobal()).partialSubmit(source.isPartialSubmit(),
         // command.isPartialSubmitSet())
         // .resetValues(source.isResetValues(), source.isResetValuesSet())
         // .ignoreAutoUpdate(source.isIgnoreAutoUpdate()).onstart(source.getOnstart()).onerror(source.getOnerror())
         // .onsuccess(source.getOnsuccess()).oncomplete(source.getOncomplete()).passParams().build();

         // script
         writer.write("<div id='" + name + "ID'>\r\n");
         writer.startElement("script", command);
         writer.writeAttribute("type", "text/javascript", null);
         writer.write(name + " = function(){" + request + "};");
         writer.write(name + "Pull = function(){");
         writer.write("injectVariableIntoScope('" + attributeName + "', '" + String.valueOf(value) + "');");
         writer.write("};\r\n");
         writer.write("addSyncPullFunction(" + name + "Pull);\r\n");

         writer.write(name + "Push = function() {");
         writer.write(name + "(");
         writer.write("[");
         // general syntax: [{name:'x', value:10}, {name:'y', value:20}]
         writer.write("{");
         writer.write("name: '" + attributeName + "', value: readVariableFromScope('" + attributeName + "')");
         writer.write("}");
         writer.write("]");
         writer.write(");");
         writer.write("\r\n");
         writer.write("};");
         writer.write("\r\n");
         writer.write("addSyncPushFunction(" + name + "Push);");

         writer.endElement("script");
         writer.write("</div>\r\n");

      }
   }
}
