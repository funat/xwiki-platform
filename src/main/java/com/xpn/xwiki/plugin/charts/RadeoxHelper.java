package com.xpn.xwiki.plugin.charts;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.radeox.macro.table.Table;
import org.radeox.macro.table.TableBuilder;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.plugin.charts.exceptions.DataSourceException;
import com.xpn.xwiki.plugin.charts.source.TableDataSource;
import com.xpn.xwiki.render.PreTagSubstitution;
import com.xpn.xwiki.render.XWikiRadeoxRenderer;
import com.xpn.xwiki.render.XWikiRenderer;
import com.xpn.xwiki.render.XWikiRenderingEngine;

public class RadeoxHelper {
	private XWikiRenderer renderer;
	private XWikiContext context;
    private XWikiDocument document;
    private static final String TABLE = "{table}";

    public RadeoxHelper(XWikiDocument document, XWikiContext context) {
        this.context = context;
        this.renderer = context.getWiki().getRenderingEngine().getRenderer("wiki");
        this.document = document;
    }

    public String getPreRadeoxContent() {
    	XWikiRenderingEngine engine = context.getWiki().getRenderingEngine();
    	Iterator it = engine.getRendererNames().iterator();
    	LinkedHashMap map = new LinkedHashMap();
    	boolean found = false;
    	while (it.hasNext()) {
    		String name = (String)it.next();    		
    		XWikiRenderer renderer = engine.getRenderer(name);
    		if (renderer instanceof XWikiRadeoxRenderer || found) {
    			found = true;
    			map.put(name, renderer);
    		}
    	}
    	
    	it = map.keySet().iterator();
    	while (it.hasNext()) {
    		engine.removeRenderer((String)it.next());
    	}
    	
    	String result;
    	try {
    		result = engine.renderDocument(document, context);
    	} catch (XWikiException e) {
    		StringWriter writer = new StringWriter();
    		e.printStackTrace(new PrintWriter(writer));
    		result = writer.toString();
    	} finally {
        	it = map.keySet().iterator();
        	while (it.hasNext()) {
        		String name = (String)it.next();
        		engine.addRenderer(name, (XWikiRenderer)map.get(name));
        	}
    	}
    	return result;
    }
    
    /**
     * @return The string content of all the the tables in the document  
     */
	public String[] getTableStrings() {
		ArrayList tables = new ArrayList();
		String content = getPreRadeoxContent();//document.getContent();

        // Remove the content that is inside "{pre}"
		content = (new PreTagSubstitution(context.getUtil(), true)).substitute(content);

		int index = Integer.MIN_VALUE;
		int lastIndex = Integer.MIN_VALUE;
		boolean opened = false;
		while (index < content.length()) {
			lastIndex = index;
			index = content.indexOf(TABLE, index+TABLE.length());
			if (index == -1) break;
			if (opened) {
				tables.add(content.substring(lastIndex+TABLE.length(), index).trim());
			}
			opened = !opened;
		}
		return (String[])tables.toArray(new String[tables.size()]);
	}

	/**
	 * @return All the radeox tables in the document
	 */
	public Table[] getTables() {
		String[] tableStrings = getTableStrings();
		Table[] tables = new Table[tableStrings.length];
		for (int i = 0; i<tableStrings.length;i++) {
			tables[i] = buildTable(tableStrings[i]);
		}
		return tables;
	}

	/**
	 * @return The string content of the given table, or null, when no such table exists 
	 */
	public String getTableString(int idx) {
		String content = getPreRadeoxContent(); //document.getContent();

        // Remove the content that is inside "{pre}"
		content = (new PreTagSubstitution(context.getUtil(), true)).substitute(content);

		int index = Integer.MIN_VALUE;
		int lastIndex = Integer.MIN_VALUE;
		int i = -1;
		boolean opened = false;
		while (index < content.length() && i<idx) {
			lastIndex = index;
			index = content.indexOf(TABLE, index+TABLE.length());
			if (index == -1) break;
			if (opened) {
				i++;
			}
			opened = !opened;
		}
		if (i == idx) {
			return content.substring(lastIndex+TABLE.length(), index).trim();
		} else {
            return null;
		}
	}

	/**
	 * @return The radeox table coresponding to the given index, or null, when no such table exists 
	 */
	public Table getTable(int idx) {
		String tableString = getTableString(idx);
		if (tableString != null) {
			return buildTable(tableString);
		} else {
			return null;
		}
	}
	
	/**
	 * @return The HTML representation of the given table-content
	 */
	public String getRenderedTable(int idx) {
		String tableString = getTableString(idx);
		if (tableString != null) {
			return renderer.render("{table}\n"+tableString+"\n{table}", null, null, context);
		} else {
			return null;
		}
	}

	private Table buildTable(String content) {
	    content = content.trim() + "\n";
	    Table table = TableBuilder.build(content);
	    table.calc();
	    return table;
	}
	
    public int getTableColumnCount(Table t) {
    	try {
    		return TableDataSource.getTableColumnCount(t);
    	} catch (DataSourceException e) {
    		return 0;
    	}

    }
    
    public int getTableRowCount(Table t){
    	try {
    		return TableDataSource.getTableRowCount(t);
    	} catch (DataSourceException e) {
    		return 0;
    	}
    }
    
    public String getCell(Table table, int columnIndex, int rowIndex) {
    	try{
    		return table.getXY(columnIndex, rowIndex).toString().replaceAll("<", "&lt;").replaceAll(">", "&gt;");
    	} catch(Exception ex){
    		return null;
    	}
    }
	
	public static String buildMacro(String name, Map params) {
		StringBuffer sb = new StringBuffer();
		sb.append("{"+name);
		if (!params.isEmpty()) {
			sb.append(":");
			Iterator it = params.keySet().iterator();
			while (it.hasNext()) {
				String paramName = (String)it.next();
				String paramValue = (String)params.get(paramName);
				sb.append(paramName+"="+paramValue);
				if (it.hasNext()) {
					sb.append("|");
				}
			}			
		}
		sb.append("}");
		return sb.toString();
	}
	
	public char getColumn(int columnIndex) {
		return (char)((int)'A' + columnIndex);
	}
}
