package com.terminaldriver.common.logger;

import org.apache.commons.text.StringEscapeUtils;
import org.tn5250j.TN5250jConstants;
import org.tn5250j.framework.tn5250.Screen5250;

import com.terminaldriver.tn5250j.annotation.ScreenAttribute;

/**
 * Utility class to log the session
 *
 * @author eberlyrh
 *
 */
public class HTMLLogger {

	static final String NEW_LINE = System.getProperty("line.separator");

	public static String getHTML(final Screen5250 screen) {
		final StringBuilder sb = new StringBuilder();
		final RowReader rowReader = new RowReader(screen);
		String row;
		sb.append("<div class=\"console\">");
		while ((row = rowReader.readRow()) != null) {
			sb.append("<span class=\"consoleline\">");
			sb.append(row);
			sb.append("</span>").append(NEW_LINE);
		}
		sb.append("</div>").append(NEW_LINE);
		return sb.toString();
	}

	public static class RowReader {
		Screen5250 screen;
		int cols;
		int pos = 0;
		String screenChars;
		String attributes;
		final boolean isattr[];

		public RowReader(final Screen5250 screen) {
			this.screen = screen;
			cols = screen.getColumns();
			screenChars = new String(screen.getScreenAsChars());
			final char attr[] = new char[screenChars.length()];
			screen.GetScreen(attr, attr.length, TN5250jConstants.PLANE_ATTR);
			attributes = new String(attr);
			final char isattr[] = new char[screenChars.length()];
			screen.GetScreen(isattr, isattr.length, TN5250jConstants.PLANE_IS_ATTR_PLACE);
			this.isattr = new boolean[screenChars.length()];
			for(int i=0;i<this.isattr.length;i++){
				this.isattr[i] = isattr[i] > 0;
			}
		}

		public String readRow() {
			if (pos + cols <= screenChars.length()) {
				final String row = screenChars.substring(pos, pos + cols);
				final String rowAttr = attributes.substring(pos, pos + cols);
				
				pos += cols;
				final StringBuilder sb = new StringBuilder();
				char currentAttr = ' ';
				ScreenAttribute currentAttrEnum = ScreenAttribute.GRN;
				sb.append("<pre>");
				sb.append("<span class=\"greenText\">");
				for (int i = 0; i < cols; i++) {
					//this should be cleaned up.  but make sure it is blank when it's an attribute.
					final boolean isAttribute = isattr[(pos-cols)+i];
					if(isAttribute){
						if(currentAttrEnum.isUnderLine()){
							sb.append("</span><span class=\"greenText\">").append(' ');
							if(currentAttr == rowAttr.charAt(i)){
								sb.append("</span><span").append(" class=\"");
								sb.append(doClass(currentAttrEnum)).append("\">");
							}
						}else{
							sb.append(' ');
						}
					}
					// The first underline is not shown
					if (currentAttr != rowAttr.charAt(i)) {
						currentAttr = rowAttr.charAt(i);
						currentAttrEnum = ScreenAttribute.getAttrEnum(currentAttr);
						sb.append("</span>").append("<span");
						sb.append(" class=\"").append(doClass(currentAttrEnum)).append("\">");
					}
					if(!isAttribute){
						if (currentAttrEnum.isNonDisplay()) {
							sb.append(" ");
						} else {
							sb.append(StringEscapeUtils.escapeHtml4(String.valueOf(row.charAt(i))));
						}
					}
				}
				sb.append("</span>");
				sb.append("</pre>");
				return sb.toString();
			}
			return null;
		}

		String doClass(final ScreenAttribute attr) {
			final StringBuilder sb = new StringBuilder();
			sb.append(attr.getColor()).append("Text");
			if (attr.isUnderLine()) {
				sb.append(" underline");
			}
			return sb.toString();
		}
	}
}
