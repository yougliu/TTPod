package com.isoftstone.tianlai.view;

import android.graphics.Camera;
import android.graphics.Matrix;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class Rotate3dAnimation extends Animation
{
  private Camera mCamera;
  private final float mCenterX;
  private final float mCenterY;
  private final float mDepthZ;
  private final float mFromDegrees;
  private final boolean mReverse;
  private final float mToDegrees;

  public Rotate3dAnimation(float paramFloat1, float paramFloat2, float paramFloat3, float paramFloat4, float paramFloat5, boolean paramBoolean)
  {
    this.mFromDegrees = paramFloat1;
    this.mToDegrees = paramFloat2;
    this.mCenterX = paramFloat3;
    this.mCenterY = paramFloat4;
    this.mDepthZ = paramFloat5;
    this.mReverse = paramBoolean;
  }

  protected void applyTransformation(float paramFloat, Transformation paramTransformation)
  {
    float f1 = this.mFromDegrees;
    float f2 = (this.mToDegrees - f1) * paramFloat;
    float f3 = f1 + f2;
    float f4 = this.mCenterX;
    float f5 = this.mCenterY;
    Camera localCamera = this.mCamera;
    Matrix localMatrix = paramTransformation.getMatrix();
    localCamera.save();
    if (this.mReverse)
    {
      float f6 = this.mDepthZ * paramFloat;
      localCamera.translate(0.0F, 0.0F, f6);
    }
    while (true)
    {
      localCamera.rotateY(f3);
      localCamera.getMatrix(localMatrix);
      localCamera.restore();
      float f7 = -f4;
      float f8 = -f5;
      boolean bool1 = localMatrix.preTranslate(f7, f8);
      boolean bool2 = localMatrix.postTranslate(f4, f5);
      float f9 = this.mDepthZ;
      float f10 = 1.0F - paramFloat;
      float f11 = f9 * f10;
      localCamera.translate(0.0F, 0.0F, f11);
      
      if(bool1 && bool2){
    	  return;
      }
    }
  }

  public void initialize(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    super.initialize(paramInt1, paramInt2, paramInt3, paramInt4);
    Camera localCamera = new Camera();
    this.mCamera = localCamera;
  }
}
