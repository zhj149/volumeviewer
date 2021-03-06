/*
 * LightDirPanel.java
 * Created on Dec 24, 2009, 11:59:50 AM
 *
 * Volume Viewer - Display and manipulate 3D volumetric data
 * Copyright © 2009, Mark McKay
 * http://www.kitfox.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.kitfox.volume.light;

import java.awt.Graphics;
import java.awt.Transparency;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import javax.vecmath.Color3f;
import javax.vecmath.Vector3f;

/**
 *
 * @author kitfox
 */
public class LightDirPanel extends javax.swing.JPanel
{
    private static final long serialVersionUID = 0;

    BufferedImage bgImage;
    private boolean positiveZ = false;
    private Color3f lightColor = new Color3f(1, 1, 1);
    protected Vector3f lightDir = new Vector3f(0, 0, -1);
    public static final String PROP_LIGHTDIR = "lightDir";

    /** Creates new form LightDirPanel */
    public LightDirPanel()
    {
        initComponents();
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);

        if (bgImage == null)
        {
            buildImage();
        }

        g.drawImage(bgImage,
                (getWidth() - bgImage.getWidth()) / 2,
                (getHeight() - bgImage.getHeight()) / 2,
                this);
    }

    private void buildImage()
    {
        int side = Math.min(getWidth(), getHeight());

        bgImage = getGraphicsConfiguration()
                .createCompatibleImage(side, side, Transparency.BITMASK);

        float radius = side / 2f;

        Vector3f p = new Vector3f();
        WritableRaster raster = bgImage.getRaster();

        for (int j = 0; j < side; ++j)
        {
            p.y = -(j - radius) / radius;
            for (int i = 0; i < side; ++i)
            {
                p.x = (i - radius) / radius;
                float len2xy = p.x * p.x + p.y * p.y;
                if (len2xy > 1)
                {
                    //Ignore outside of circle
                    continue;
                }

                p.z = (float)Math.sqrt(1 - len2xy);
                if (!positiveZ)
                {
                    p.z = -p.z;
                }

                float lum = p.dot(lightDir);
                if (lum < 0)
                {
                    lum = 0;
                }

                raster.setSample(i, j, 0, 255 * (lum * lightColor.x));
                raster.setSample(i, j, 1, 255 * (lum * lightColor.y));
                raster.setSample(i, j, 2, 255 * (lum * lightColor.z));
                raster.setSample(i, j, 3, 255);
            }
        }
    }

    private void updateLightDir(MouseEvent evt)
    {
        int side = Math.min(getWidth(), getHeight());
        float radius = side / 2f;
        float offx = getWidth() / 2 - radius;
        float offy = getHeight() / 2 - radius;
        Vector3f newLightDir = new Vector3f();
        newLightDir.x = (evt.getX() - offx - radius) / radius;
        newLightDir.y = -(evt.getY() - offy - radius) / radius;
        newLightDir.z = 0;

        if (newLightDir.lengthSquared() >= 1)
        {
            newLightDir.normalize();
        }
        else
        {
            newLightDir.z = (float)Math.sqrt(1 - newLightDir.lengthSquared());
            if (!positiveZ)
            {
                newLightDir.z = -newLightDir.z;
            }
        }

        setLightDir(newLightDir);
    }

    private void update()
    {
        bgImage = null;
        repaint();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setToolTipText("Click to change light direction");
        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                formMouseReleased(evt);
            }
        });
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
        });
        addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                formMouseDragged(evt);
            }
        });
    }// </editor-fold>//GEN-END:initComponents

    private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
        bgImage = null;
    }//GEN-LAST:event_formComponentResized

    private void formMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseReleased
        updateLightDir(evt);
    }//GEN-LAST:event_formMouseReleased

    private void formMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseDragged
        updateLightDir(evt);
    }//GEN-LAST:event_formMouseDragged

    /**
     * @return the positiveZ
     */
    public boolean isPositiveZ() {
        return positiveZ;
    }

    /**
     * @param positiveZ the positiveZ to set
     */
    public void setPositiveZ(boolean positiveZ) {
        this.positiveZ = positiveZ;
        update();
    }

    /**
     * Get the value of lightDir
     *
     * @return the value of lightDir
     */
    public Vector3f getLightDir() {
        return new Vector3f(lightDir);
    }

    /**
     * Set the value of lightDir
     *
     * @param lightDir new value of lightDir
     */
    public void setLightDir(Vector3f lightDir) {
//        if (this.lightDir.equals(lightDir))
//        {
//            return;
//        }
        Vector3f oldLightDir = new Vector3f(this.lightDir);
        this.lightDir.set(lightDir);
        update();
        firePropertyChange(PROP_LIGHTDIR, oldLightDir, lightDir);
    }

    /**
     * @return the lightColor
     */
    public Color3f getLightColor() {
        return lightColor;
    }

    /**
     * @param lightColor the lightColor to set
     */
    public void setLightColor(Color3f lightColor) {
//        if (this.lightColor.equals(lightColor))
//        {
//            return;
//        }
        this.lightColor = lightColor;
        update();
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

}
