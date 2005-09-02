package com.xpn.xwiki.plugin.charts.plots;

import java.lang.reflect.Constructor;

import org.jfree.chart.plot.Plot;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;

import com.xpn.xwiki.plugin.charts.ChartCustomizer;
import com.xpn.xwiki.plugin.charts.exceptions.DataSourceException;
import com.xpn.xwiki.plugin.charts.exceptions.GenerateException;
import com.xpn.xwiki.plugin.charts.params.ChartParams;
import com.xpn.xwiki.plugin.charts.source.DataSource;

public class LinePlotFactory  implements PlotFactory {
	private static LinePlotFactory uniqueInstance = new LinePlotFactory();
	
	private LinePlotFactory() {
		// empty
	}

	public static LinePlotFactory getInstance() {
		return uniqueInstance;
	}
	
	public Plot create(DataSource dataSource, ChartParams params)
			throws GenerateException, DataSourceException {
		Class rendererClass = params.getClass(ChartParams.RENDERER);
		if (rendererClass == null || XYItemRenderer.class.isAssignableFrom(rendererClass)) {
			XYItemRenderer renderer;
			if (rendererClass != null) {
				try {
					Constructor ctor = rendererClass.getConstructor(new Class[] {});
					renderer = (XYItemRenderer)ctor.newInstance(new Object[] { });
				} catch (Throwable e) {
					throw new GenerateException(e);
				}
			} else {
				renderer = new XYLineAndShapeRenderer();
			}
			ChartCustomizer.customizeXYItemRenderer(renderer, params);

			return XYPlotFactory.getInstance().create(dataSource, renderer, params);
		} else if (CategoryItemRenderer.class.isAssignableFrom(rendererClass)) {
			CategoryItemRenderer renderer;
			if (rendererClass != null) {
				try {
					Constructor ctor = rendererClass.getConstructor(new Class[] {});
					renderer = (CategoryItemRenderer)ctor.newInstance(new Object[] { });
				} catch (Throwable e) {
					throw new GenerateException(e);
				}
			} else {
				renderer = new LineAndShapeRenderer();
			}
			ChartCustomizer.customizeCategoryItemRenderer(renderer, params);
			
			return CategoryPlotFactory.getInstance().create(dataSource, renderer, params);
		} else {
			throw new GenerateException("Incompatible renderer class: " + rendererClass);
		}
	}
	
//	private boolean hasNumericHeader(DataSource dataSource,
//			ChartParams params) throws DataSourceException {
//		String dataSeries = params.getString(ChartParams.SERIES);
//		try { 
//			if (dataSeries.equals("rows") && dataSource.hasHeaderRow()) {
//				for (int column = 0; column<dataSource.getColumnCount(); column++) {
//					Double.parseDouble(dataSource.getHeaderRowValue(column));
//				}
//				return true;
//			} else if (dataSeries.equals("columns") && dataSource.hasHeaderColumn()) {
//				for (int row = 0; row<dataSource.getRowCount(); row++) {
//					Double.parseDouble(dataSource.getHeaderColumnValue(row));
//				}
//				return true;			
//			}
//		} catch (NumberFormatException e) { /* ignore */ }
//		return false;
//	}
}
