/*
 * @(#)Matrix3D.java   06/22/97
 *
 * Copyright (c) 2000 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights Reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Visit eTeks web site for up-to-date versions of this file and other
 * Java tools and tutorials : http://www.eteks.com/
 */
package com.eteks.tools.d3d;

/**
 * 3D matrix.
 *
 * @version 1.0
 * @author  Emmanuel Puybaret
 * @since   Jeks 1.0
 */
public class Matrix3D
{
  protected float coord [][];

  public Matrix3D ()
  {
    coord = new float [4][];

    for (int axe = 0; axe < 4; axe++)
      coord [axe] = new float [4];
  }

  public String toString ()
  {
    String retour = "Matrix3D :\n";
    for (int i = 0; i < 4; i++)
    {
      retour += "line " + i + " :";
      for (int j = 0; j < 4; j++)
        retour += " " +  coord [i][j];
      if (i < 3)
        retour += "\n";
    }
    return retour;
  }

  public Matrix3D multiplyMatrix (Matrix3D matrix)
  {
    Matrix3D matriceMult = new Matrix3D ();

    for (int i = 0; i < 4; i++)
      for (int j = 0; j < 4; j++)
        for (int k = 0; k < 4; k++)
          matriceMult.coord [i][j] += coord [i][k] * matrix.coord [k] [j];

    return matriceMult;
  }

  public float getValue (int i, int j)
  {
    return coord [i][j];
  }

  static public Matrix3D getIndentity3DMatrix ()
  {
    Matrix3D matrix = new Matrix3D ();
    for (int i = 0; i < 4; i++)
      matrix.coord [i][i] = 1;

    return matrix;
  }

  static public Matrix3D getRotationMatrix (Point3D  centreRot,
                                            Vector3D angleRot,
                                            int []   ordreRot)
  {
    Matrix3D matriceRot = Matrix3D.getIndentity3DMatrix ();
    Matrix3D matriceTemp;
    int       axe;

   for (axe = 0; axe <= 2; axe++)
     matriceRot.coord [axe][3] = centreRot.getCoordinate (axe);

   for (int i = 2; i >= 0; i--)
     if (angleRot.getCoordinate (axe = ordreRot [i]) != 0.)
     {
        matriceTemp = Matrix3D.getIndentity3DMatrix ();

        matriceTemp.coord [(axe + 1) % 3][(axe + 1) % 3] =
        matriceTemp.coord [(axe + 2) % 3][(axe + 2) % 3] =
          (float)Math.cos (angleRot.getCoordinate (axe));

        matriceTemp.coord [(axe + 1) % 3][(axe + 2) % 3] =
        -(matriceTemp.coord [(axe + 2) % 3][(axe + 1) % 3] =
           (float)Math.sin (angleRot.getCoordinate (axe)));

       matriceRot = matriceRot.multiplyMatrix (matriceTemp);
     } /** end if **/

    matriceTemp = Matrix3D.getIndentity3DMatrix ();
    for (axe = 0; axe <= 2; axe++)
      matriceTemp.coord [axe][3] = -centreRot.getCoordinate (axe);

    return (matriceRot.multiplyMatrix (matriceTemp));
  } // end getRotationMatrix ()


  static public Matrix3D getTranslationMatrix (Vector3D translation)
  {
    Matrix3D matriceTransl = Matrix3D.getIndentity3DMatrix ();

    for (int axe = 0; axe <= 2; axe++)
      matriceTransl.coord [axe][3] = translation.getCoordinate (axe);
    return (matriceTransl);
  } // end getTranslationMatrix ()

  static public Matrix3D getProjectionMatrix (float rapport,
                                              float angle,
                                              float distance)
  {
    Matrix3D matriceProj = Matrix3D.getIndentity3DMatrix ();

    matriceProj.coord [0][2] = -rapport * (float)Math.cos (angle);
    matriceProj.coord [1][2] = -rapport * (float)Math.sin (angle);
    if (distance != 0.)
      matriceProj.coord [3][2] = -1 / distance;

    return (matriceProj);
  } // end getProjectionMatrix ()


  static public Matrix3D getScaleMatrix (Vector3D echelle)
  {
     Matrix3D matriceEch = Matrix3D.getIndentity3DMatrix ();

     for (int axe = 0; axe <= 2; axe++)
       matriceEch.coord [axe][axe] = echelle.getCoordinate (axe);

     return (matriceEch);
   } // end getScaleMatrix ()

  static public Matrix3D getDisplayMatrix (Point3D  centreRotation,
                                           Vector3D angleRotation,
                                           int      ordreRotation [],
                                           Vector3D translation,
                                           float    rapportProjection,
                                           float    angleProjection,
                                           float    distance,
                                           Point3D  centreRepere,
                                           Vector3D echelle)
  {
    Matrix3D matriceVisualisation =
       getScreenMatrix (rapportProjection, angleProjection,
                        distance, centreRepere, echelle)
            .multiplyMatrix (getReferenceMatrix (centreRotation, angleRotation,
                                                 ordreRotation, translation));
    return matriceVisualisation;
  } // end getDisplayMatrix ()

  static public Matrix3D getReferenceMatrix (Point3D  centreRotation,
                                             Vector3D angleRotation,
                                             int      ordreRotation [],
                                             Vector3D translation)
  {
    Matrix3D matriceRepere =  getTranslationMatrix (translation)
                                .multiplyMatrix (getRotationMatrix (centreRotation,
                                                 			        angleRotation,
                                                				    ordreRotation));
    return matriceRepere;
  } // end getReferenceMatrix ()

  static public Matrix3D getScreenMatrix (float    rapportProjection,
                                          float    angleProjection,
                                          float    distance,
                                          Point3D  centreRepere,
                                          Vector3D echelle)
  {
    Matrix3D matriceEcran =
         getTranslationMatrix (new Vector3D (centreRepere))
          .multiplyMatrix (getScaleMatrix (echelle))
            .multiplyMatrix (getProjectionMatrix (rapportProjection,
                                                  angleProjection,
                                                  distance));
    return matriceEcran;
  } // end getScreenMatrix ()
}
