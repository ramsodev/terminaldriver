package com.terminaldriver.tn5250j.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.text.StringEscapeUtils;
import org.tn5250j.framework.tn5250.Screen5250;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.terminaldriver.tn5250j.annotation.ScreenAttribute;
import com.terminaldriver.tn5250j.obj.ScreenDataContainer;

public class ScreenSerializer {

	final int colCount;
	final ScreenDataContainer screenContainer;
	int curPosition = 0;

	public ScreenSerializer(final Screen5250 screen) {
		screenContainer = new ScreenDataContainer(screen);
		colCount = screen.getColumns();
	}

	public String serialize(final Screen5250 screen, final boolean indent) throws JsonProcessingException {
		curPosition = 0;
		final List<Map<String, Object>> maps = serializeToMaps(screen);
		final Map<String, Object> screenMap = new HashMap<String, Object>();
		screenMap.put("fields", maps);
		screenMap.put("columns", screen.getColumns());
		screenMap.put("rows", screen.getRows());
		final ObjectMapper mapper = new ObjectMapper();
		if (indent) {
			mapper.enable(SerializationFeature.INDENT_OUTPUT);
		}
		return mapper.writeValueAsString(screenMap);
	}

	public String serializeXML(final Screen5250 screen) {
		curPosition = 0;
		final List<Map<String, Object>> maps = serializeToMaps(screen);
		final StringBuilder sb = new StringBuilder();
		sb.append(String.format("<screen rows=\"%s\" columns=\"%s\">\r\n",screen.getRows(),screen.getColumns()));
		for (final Map<String, Object> map : maps) {
			sb.append("  <field");
			for (final String col : Arrays.asList("text", "row", "col", "attr", "field", "length", "rawtext",
					"isAttrOffset")) {
				if (map.containsKey(col)) {
					sb.append(" ").append(col).append("=\"")
							.append(StringEscapeUtils.escapeXml10(map.get(col).toString())).append("\"");
				}
			}
			sb.append("/>\r\n");
		}
		sb.append("</screen>\r\n");
		return sb.toString();
	}

	public List<Map<String, Object>> serializeToMaps(final Screen5250 screen) {
		final List<Map<String, Object>> retval = new ArrayList<Map<String, Object>>();
		Map<String, Object> field;
		while ((field = readField()) != null) {
			retval.add(field);
		}
		return retval;
	}

	public Map<String, Object> readField() {
		while (curPosition < screenContainer.text.length) {
			final int curRow = ScreenUtils.pos2row(curPosition, colCount);

			final int savePos = curPosition;
			final char currentAttr = screenContainer.attr[curPosition];
			final HashMap<String, Object> retval = new HashMap<String, Object>();
			final StringBuilder text = new StringBuilder();
			retval.put("row", curRow);

			if (currentAttr != ' ') {
				retval.put("attr", ScreenAttribute.getAttrEnum(currentAttr).toString());
			}
			if (screenContainer.isAttr[curPosition] == 0) {
				retval.put("isAttrOffset", "N");
			}
			curPosition++;
			int curCol = ScreenUtils.pos2col(curPosition+1, colCount);
			retval.put("col", curCol);
			if (curPosition < screenContainer.field.length) {
				final int fieldAttr = screenContainer.field[curPosition];
				if (fieldAttr > 0) {
					retval.put("field", fieldAttr);
				}
			}

			while (curRow == ScreenUtils.pos2row(curPosition, colCount) && screenContainer.isAttr[curPosition] == 0
					&& screenContainer.attr[curPosition] == currentAttr) {
				text.append(screenContainer.text[curPosition++]);
			}

			final String scrubbedText = text.toString().replace((char) 0, ' ').replaceFirst("\\s+$", "");
			retval.put("text", scrubbedText);
			int length = curPosition - savePos;
			// When wrapped trim 1
			if (curRow != ScreenUtils.pos2row(curPosition, colCount)) {
				length--;
			}
			retval.put("length", length);

			if (retval.containsKey("field")) {
				retval.put("length", length - 1);
				// retval.put("col", curCol+1);
				if (!scrubbedText.equals(text.toString().replaceFirst("\0+$", ""))) {
					retval.put("rawtext", text.toString().replaceFirst("\0+$", ""));
				}
			} else {
				if (!scrubbedText.equals(text.toString().replaceFirst("\\s+$", ""))) {
					retval.put("rawtext", text.toString().replaceFirst("\\s+$", ""));
				}
			}
			if (isNotBlank(retval)) {
				return retval;
			}
		}

		return null;
	}

	private boolean isNotBlank(final HashMap<String, Object> map) {
		return !(map.get("text").toString().replace(' ', 'x').replace((char) 0, ' ').trim().isEmpty()
				&& !map.containsKey("attr") && !map.containsKey("field"));
	}

}
