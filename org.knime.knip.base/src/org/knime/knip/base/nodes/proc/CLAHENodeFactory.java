/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2013
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
 * --------------------------------------------------------------------- *
 *
 */
package org.knime.knip.base.nodes.proc;

import java.util.List;

import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.meta.ImgPlus;
import net.imglib2.ops.operation.Operations;
import net.imglib2.ops.operation.UnaryOutputOperation;
import net.imglib2.type.numeric.RealType;

import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelInteger;
import org.knime.knip.base.node.ImgPlusToImgPlusNodeDialog;
import org.knime.knip.base.node.ImgPlusToImgPlusNodeFactory;
import org.knime.knip.base.node.ImgPlusToImgPlusNodeModel;
import org.knime.knip.core.ops.img.ImgPlusToImgPlusWrapperOp;
import org.knime.knip.core.util.ImgPlusFactory;

/**
 * Factory class to produce an image inverter node.
 *
 * @author <a href="mailto:dietzc85@googlemail.com">Christian Dietz</a>
 * @author <a href="mailto:horn_martin@gmx.de">Martin Horn</a>
 * @author <a href="mailto:michael.zinsmaier@googlemail.com">Michael Zinsmaier</a>
 * @param <K>
 */
public class CLAHENodeFactory<T extends RealType<T>, K extends RandomAccessibleInterval<T> & IterableInterval<T>> extends ImgPlusToImgPlusNodeFactory<T, T> {

    private static SettingsModelInteger createCtxRegionXModel() {
        return new SettingsModelInteger("number_context_regions_X", 8);
    }

    private static SettingsModelInteger createCtxRegionYModel() {
        return new SettingsModelInteger("number_context_regions_Y", 8);
    }

    private static SettingsModelBoolean createEnableClippingModel() {
        return new SettingsModelBoolean("enable_clipping", true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ImgPlusToImgPlusNodeDialog<T> createNodeDialog() {
        return new ImgPlusToImgPlusNodeDialog<T>(2, 2, "X", "Y") {

            @Override
            public void addDialogComponents() {

                addDialogComponent("Options", "CLAHE options", new DialogComponentNumber(createCtxRegionXModel(),
                        "Number of contextual regions in X direction.", 1));

                addDialogComponent("Options", "CLAHE options", new DialogComponentNumber(createCtxRegionYModel(),
                        "Number of contextual regions in Y direction.", 1));

                addDialogComponent("Options", "CLAHE options", new DialogComponentBoolean(createEnableClippingModel(),
                        "Clipping"));

            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ImgPlusToImgPlusNodeModel<T, T> createNodeModel() {
        return new ImgPlusToImgPlusNodeModel<T, T>("X", "Y") {

            private final SettingsModelInteger m_ctxRegionsX = createCtxRegionXModel();

            private final SettingsModelInteger m_ctxRegionsY = createCtxRegionYModel();

            private final SettingsModelBoolean m_enableClipping = createEnableClippingModel();

            @Override
            protected void addSettingsModels(final List<SettingsModel> settingsModels) {
                settingsModels.add(m_ctxRegionsX);
                settingsModels.add(m_ctxRegionsY);
                settingsModels.add(m_enableClipping);
            }

            @Override
            protected UnaryOutputOperation<ImgPlus<T>, ImgPlus<T>> op(final ImgPlus<T> imgPlus) {

//                CLAHE<T, K> clahe =
//                        new CLAHE<T, K>(m_ctxRegionsX.getIntValue(), m_ctxRegionsY.getIntValue(),
//                                m_enableClipping.getBooleanValue());

                Clahe_new<T> clahe = new Clahe_new<T>(127, 255, 3);

                return Operations.wrap(new ImgPlusToImgPlusWrapperOp<T, T>(clahe, imgPlus.firstElement()
                        .createVariable()), ImgPlusFactory.<T, T> get(imgPlus.firstElement()));
            }

            @Override
            protected int getMinDimensions() {
                return 2;
            }
        };
    }
}
