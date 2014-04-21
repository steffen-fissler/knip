/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2014
 *  University of Konstanz, Germany and
 *  KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ---------------------------------------------------------------------
 *
 * Created on 21.01.2014 by Andreas
 */
package org.knime.knip.base.nodes.testing.TableCellViewer;

import java.awt.Component;

import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import org.knime.core.data.DataValue;
import org.knime.core.node.config.ConfigRO;
import org.knime.core.node.config.ConfigWO;
import org.knime.knip.base.KNIMEKNIPPlugin;
import org.knime.knip.base.data.img.ImgPlusValue;
import org.knime.knip.base.nodes.view.TableCellView;
import org.knime.knip.base.nodes.view.TableCellViewFactory;
import org.knime.knip.core.awt.ImageRenderer;
import org.knime.knip.core.awt.RendererFactory;
import org.knime.knip.core.ui.event.EventService;
import org.knime.knip.core.ui.imgviewer.ImgViewer;
import org.knime.knip.core.ui.imgviewer.events.ImgRedrawEvent;
import org.knime.knip.core.ui.imgviewer.events.PlaneSelectionEvent;
import org.knime.knip.core.ui.imgviewer.events.RendererSelectionChgEvent;
import org.knime.knip.core.ui.imgviewer.events.ResetCacheEvent;
import org.knime.knip.core.ui.imgviewer.events.ViewClosedEvent;
import org.knime.knip.core.ui.imgviewer.events.ViewZoomfactorChgEvent;
import org.knime.knip.core.ui.imgviewer.panels.MinimapPanel;

/**
 *
 * @author Andreas Burger
 */
public class TestImgCellViewFactory<T extends RealType<T> & NativeType<T>> implements TableCellViewFactory {

    /**
     * {@inheritDoc}
     */
    @Override
    public TableCellView[] createTableCellViews() {
        return new TableCellView[]{new TableCellView() {

            private ImgViewer m_view = null;

            /**
             * {@inheritDoc}
             */
            @Override
            public String getDescription() {
                return "Logging image view.";
            }

            @Override
            public String getName() {
                return "Image Viewer Test View";
            }

            @Override
            public Component getViewComponent() {
                if (m_view == null) {
                    m_view = TestViewerFactory.createImgViewer(KNIMEKNIPPlugin.getCacheSizeForBufferedImages());
                }

                return m_view;
            }

            @Override
            public void loadConfigurationFrom(final ConfigRO config) {
                //

            }

            @Override
            public void onClose() {
                m_view.getEventService().publish(new ViewClosedEvent());
            }

            @Override
            public void onReset() {
                m_view.getEventService().publish(new ResetCacheEvent());
            }

            @Override
            public void saveConfigurationTo(final ConfigWO config) {
                //

            }

            @Override
            public void updateComponent(final DataValue valueToView) {
                @SuppressWarnings("unchecked")
                final ImgPlusValue<T> imgPlusValue = (ImgPlusValue<T>)valueToView;
                m_view.setImg(imgPlusValue.getImgPlus());
                testComponent(imgPlusValue);
            }


            /**
             * This method handles the actual testing.
             * @param imgPlusValue The Image to be displayed, used in tests.
             */
            private void testComponent(final ImgPlusValue<T> imgPlusValue) {
                EventService service = m_view.getEventService();

                //              Calibration

                service.publish(new ImgWithMetadataChgEvent(imgPlusValue.getImgPlus(), imgPlusValue.getMetadata()));
                service.publish(new TestCompleteEvent());

                //              Normalization

                service.publish(new NormalizationParametersChgEvent(0, false));
                service.publish(new TestCompleteEvent());

                service.publish(new NormalizationParametersChgEvent(25, true));
                service.publish(new TestCompleteEvent());

                service.publish(new NormalizationParametersChgEvent(50, true));
                service.publish(new TestCompleteEvent());

                //                Minimap

                service.publish(new ViewZoomfactorChgEvent(MinimapPanel.ZOOM_MIN / 100d));
                service.publish(new ImgRedrawEvent());
                service.publish(new TestCompleteEvent());

                service.publish(new ViewZoomfactorChgEvent((MinimapPanel.ZOOM_MIN + MinimapPanel.ZOOM_MAX) / 200d));
                service.publish(new ImgRedrawEvent());
                service.publish(new TestCompleteEvent());

                service.publish(new ViewZoomfactorChgEvent(MinimapPanel.ZOOM_MAX / 100d));
                service.publish(new ImgRedrawEvent());
                service.publish(new TestCompleteEvent());

                //                PlaneSelection

                if (imgPlusValue.getMetadata().numDimensions() > 1) {

                    for (int i = 2; i < imgPlusValue.getMetadata().numDimensions(); ++i) {
                        if (imgPlusValue.getDimensions()[i] > 1) {
                            long[] tempcords = new long[imgPlusValue.getMetadata().numDimensions()];
                            tempcords[i] = 1;
                            service.publish(new PlaneSelectionEvent(0, i, tempcords));
                            service.publish(new ImgRedrawEvent());
                            service.publish(new TestCompleteEvent());
                        }
                    }
                }

                //              RendererSelection

                ImageRenderer<T>[] tmp = RendererFactory.createSuitableRenderer(imgPlusValue.getImgPlus());

                for (ImageRenderer<T> ir : tmp) {
                    service.publish(new RendererSelectionChgEvent(ir));
                    service.publish(new ImgRedrawEvent());
                    service.publish(new TestCompleteEvent());
                }

            }

        }, new TableCellView() {
            private ImgViewer m_view = null;

            @Override
            public String getDescription() {
                return "";
            }

            @Override
            public String getName() {
                return "Test Histogram";
            }

            @Override
            public Component getViewComponent() {
                if (m_view == null) {
                    m_view = TestViewerFactory.createHistViewer(KNIMEKNIPPlugin.getCacheSizeForBufferedImages());
                }

                return m_view;
            }

            @Override
            public void loadConfigurationFrom(final ConfigRO config) {
                //

            }

            @Override
            public void onClose() {
                m_view.getEventService().publish(new ViewClosedEvent());
            }

            @Override
            public void onReset() {
                m_view.getEventService().publish(new ResetCacheEvent());
            }

            @Override
            public void saveConfigurationTo(final ConfigWO config) {
                //

            }

            @Override
            public void updateComponent(final DataValue valueToView) {
                final ImgPlusValue<T> imgPlusValue = (ImgPlusValue<T>)valueToView;
                m_view.setImg(imgPlusValue.getImgPlus());
                testComponent(imgPlusValue);
            }

            private void testComponent(final ImgPlusValue<T> imgPlusValue) {
                EventService service = m_view.getEventService();

//              HistogramRU reacts only to PlaneSelectionEvents

//              PlaneSelection

              if (imgPlusValue.getMetadata().numDimensions() > 1) {


                  for (int i = 2; i < imgPlusValue.getMetadata().numDimensions(); ++i) {
                      if (imgPlusValue.getDimensions()[i] > 1) {
                          long[] tempcords = new long[imgPlusValue.getMetadata().numDimensions()];
                          tempcords[i] = 1;
                          service.publish(new PlaneSelectionEvent(0, i, tempcords));
                          service.publish(new ImgRedrawEvent());
                          service.publish(new TestCompleteEvent());
                      }
                  }
              }
            }

        }};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<? extends DataValue> getDataValueClass() {
        return ImgPlusValue.class;
    }

}
