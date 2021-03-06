/*
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

package com.kitfox.volume.viewer;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

/**
 *
 * @author kitfox
 */
public class DataSamplerImage extends DataSampler
{
    float[] values;
    float[] valuesDx;
    float[] valuesDy;
    float[] valuesDz;
    private final int xSpan;
    private final int ySpan;
    private final int zSpan;

    public DataSamplerImage(BufferedImage[] img)
    {
        xSpan = img[0].getWidth();
        ySpan = img[0].getHeight();
        zSpan = img.length;
        
        int size = xSpan * ySpan * zSpan;
        System.err.println("Mem needed " + (size * 4 * 4));
        values = new float[size];
        valuesDx = new float[size];
        valuesDy = new float[size];
        valuesDz = new float[size];


        //Pull values from images
        for (int k = 0; k < zSpan; ++k)
        {
            WritableRaster raster = img[k].getRaster();
            if (raster.getWidth() != xSpan
                    || raster.getHeight() != ySpan)
            {
                throw new IllegalArgumentException("All images must be the same size");
            }

            for (int j = 0; j < ySpan; ++j)
            {
                for (int i = 0; i < xSpan; ++i)
                {
                    values[index(i, j, k)]
                            = raster.getSample(i, ySpan - j - 1, 0) / 255f;
                }
            }
        }

        for (int k = 0; k < zSpan; ++k)
        {
            for (int j = 0; j < ySpan; ++j)
            {
                for (int i = 0; i < xSpan; ++i)
                {
                    int idx = (k * ySpan + j) * xSpan + i;
                    valuesDx[idx] = (sampleRaw(i + 1, j, k)
                            - sampleRaw(i - 1, j, k)) / 2;
                    valuesDy[idx] = (sampleRaw(i, j + 1, k)
                            - sampleRaw(i, j - 1, k)) / 2;
                    valuesDz[idx] = (sampleRaw(i, j, k + 1)
                            - sampleRaw(i, j, k - 1)) / 2;
                }
            }
        }
    }

    private float sampleRaw(int i, int j, int k)
    {
        try
        {
            return values[index(i, j, k)];
        }
        catch (ArrayIndexOutOfBoundsException ex)
        {
            throw ex;
        }
    }

    private int index(int x, int y, int z)
    {
        x = Math.min(Math.max(x, 0), getxSpan() - 1);
        y = Math.min(Math.max(y, 0), getySpan() - 1);
        z = Math.min(Math.max(z, 0), getzSpan() - 1);
        return (z * getySpan() + y) * getxSpan() + x;
    }

    private float getSample(float[] values, float x, float y, float z)
    {
        //Find cell and offset into cell to evaluate
        float offx = x * getxSpan();
        int x0 = (int)offx;
        float xf = offx - x0;

        float offy = y * getySpan();
        int y0 = (int)offy;
        float yf = offy - y0;

        float offz = z * getzSpan();
        int z0 = (int)offz;
        float zf = offz - z0;

//        if (true)
//        {
            //Debugging bypass of cubic interpolation
//            return values[index(x0, y0, z0)];
//        }

        return lerpCell(values, x0, y0, z0, xf, yf, zf);

        //Interpolate cell
//        return cubicInterpCell(values, x0, y0, z0, xf, yf, zf);
    }

    private float lerpCell(float[] arr, int x, int y, int z, float ax, float ay, float az)
    {
        float p000 = arr[index(x, y, z)];
        float p100 = arr[index(x + 1, y, z)];
        float p010 = arr[index(x, y + 1, z)];
        float p110 = arr[index(x + 1, y + 1, z)];
        float p001 = arr[index(x, y, z + 1)];
        float p101 = arr[index(x + 1, y, z + 1)];
        float p011 = arr[index(x, y + 1, z + 1)];
        float p111 = arr[index(x + 1, y + 1, z + 1)];

        float px00 = lerp(p000, p100, ax);
        float px10 = lerp(p010, p110, ax);
        float px01 = lerp(p001, p101, ax);
        float px11 = lerp(p011, p111, ax);

        float pxy0 = lerp(px00, px10, ay);
        float pxy1 = lerp(px01, px11, ay);

        return lerp(pxy0, pxy1, az);
    }

    private float lerp(float p0, float p1, float alpha)
    {
        return p0 * (1 - alpha) + p1 * alpha;
    }

    private float cubicInterpCell(float[] arr, int x, int y, int z, float ax, float ay, float az)
    {
        float[] zValues = new float[16];

        //Find z values
        for (int j = 0; j < 4; ++j)
        {
            for (int i = 0; i < 4; ++i)
            {
                zValues[i + j * 4] = cubicInterp(
                        arr[index(x + i - 1, y + j - 1, z - 1)],
                        arr[index(x + i - 1, y + j - 1, z)],
                        arr[index(x + i - 1, y + j - 1, z + 1)],
                        arr[index(x + i - 1, y + j - 1, z + 2)],
                        az);
            }
        }

        float[] yValues = new float[4];
        //Find y values
        for (int i = 0; i < 4; ++i)
        {
            yValues[i] = cubicInterp(
                    zValues[i],
                    zValues[i + 4],
                    zValues[i + 8],
                    zValues[i + 12],
                    ay);
        }

        return cubicInterp(
                yValues[0],
                yValues[1],
                yValues[2],
                yValues[3],
                ax);
    }

    private float cubicInterp(float v0, float v1, float v2, float v3, float alpha)
    {
        //Values
        float f0 = v1;
        float f1 = v2;

        //Derivatives
        float df0 = (v2 - v0) / 2;
        float df1 = (v3 - v1) / 2;

        //Calc coefficients
        float a = 2 * f0 - 2 * f1 + df0 + df1;
        float b = -3 * f0 + 3 * f1 - 2 * df0 - df1;
        float c = df0;
        float d = f0;

        //Do interpolation
        return ((a * alpha + b) * alpha + c) * alpha + d;
    }

    public float getValue(float x, float y, float z)
    {
        return getSample(values, x, y, z);
    }

    public float getDx(float x, float y, float z)
    {
        return getSample(valuesDx, x, y, z);
    }

    public float getDy(float x, float y, float z)
    {
        return getSample(valuesDy, x, y, z);
    }

    public float getDz(float x, float y, float z)
    {
        return getSample(valuesDz, x, y, z);
    }

    /**
     * @return the xSpan
     */
    public int getxSpan() {
        return xSpan;
    }

    /**
     * @return the ySpan
     */
    public int getySpan() {
        return ySpan;
    }

    /**
     * @return the zSpan
     */
    public int getzSpan() {
        return zSpan;
    }

}
