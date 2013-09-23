/*
 * Copyright 2012 Vaadin Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.vaadin.tori.component.post.editor;

import java.util.ArrayList;
import java.util.List;

import org.vaadin.openesignforms.ckeditor.CKEditorConfig;
import org.vaadin.openesignforms.ckeditor.CKEditorTextField;
import org.vaadin.tori.ToriUI;
import org.vaadin.tori.util.PostFormatter.FontsInfo;
import org.vaadin.tori.util.PostFormatter.FontsInfo.FontFace;
import org.vaadin.tori.util.PostFormatter.FontsInfo.FontSize;

import com.vaadin.server.VaadinService;

@SuppressWarnings("serial")
public class BBCodeWysiwygEditor extends CKEditorTextField {

    public BBCodeWysiwygEditor(final String captionText) {
        addStyleName("wysiwyg-editor");
        setCaption(captionText);
        setHeight(null);

        CKEditorConfig config = new CKEditorConfig();
        setConfig(config);

        String themesPath = VaadinService.getCurrentRequest().getContextPath()
                + "/VAADIN/themes/";
        String toriTheme = "tori/";
        String toriCss = themesPath + toriTheme + "styles.css";
        String threadViewCss = themesPath + toriTheme + "threadview/style.css";
        String editorCss = themesPath + toriTheme + "editor/editor.css";
        config.setBodyClass("v-app v-widget authoring");

        config.setContentsCss(toriCss, threadViewCss, editorCss);
        config.addToExtraPlugins("bbcode");
        config.addToExtraPlugins("codebutton");
        config.addCustomToolbarLine("{ items: ['Font','FontSize'] },"
                + "{ items: ['Bold','Italic','Underline','Strike','TextColor','RemoveFormat'] },"
                + "{ items: ['codebutton'] },"
                + "{ items: ['Link','Image','NumberedList','BulletedList'] },"
                + "{ items: ['Source'] }");
        FontsInfo fontsInfo = ToriUI.getCurrent().getPostFormatter()
                .getFontsInfo();
        List<String> fontNames = new ArrayList<String>();
        for (FontFace ff : fontsInfo.getFontFaces()) {
            fontNames.add(ff.getFontName());
        }
        config.setFontNames(fontNames);
        config.setForcePasteAsPlainText(true);
        config.disableElementsPath();

        config.setEnterMode(1);

        StringBuilder sb = new StringBuilder();
        for (FontSize fs : fontsInfo.getFontSizes()) {
            if (!sb.toString().isEmpty()) {
                sb.append(";");
            }
            sb.append(fs.getFontSizeName()).append("/")
                    .append(fs.getFontSizeSyntax());
        }
        sb.append("'").insert(0, "'");

        // config.addExtraConfig("fontSize_sizes", sb.toString());
        config.addExtraConfig("fontSize_sizes",
                "'1/0.7em;2/0.8em;3/0.9em;4/1em;5/1.1em;6/1.3em;7/1.5em'");

    }
}