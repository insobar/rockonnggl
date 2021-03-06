/*
 * 
 */

package org.abrantix.rockon.rockonnggl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.database.CharArrayBuffer;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.GLUtils;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;

//public abstract class RockOnRenderer implements GLSurfaceView.Renderer{
public abstract class RockOnRenderer{

	final String TAG = this.toString();
	Handler	mRequestRenderHandler;
	
	public void renderNow(){
		mRequestRenderHandler.sendEmptyMessage(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
	}
	
	public void stopRender()
	{
		mRequestRenderHandler.sendEmptyMessage(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
	}
	
	abstract public int getType();
	
	abstract public void clearCache();
    
	abstract public void changeBrowseCat(int browseCat);
	
	abstract public int	 getBrowseCat();
	
    abstract public void changePlaylist(int playlistId);
    
    abstract public	Cursor getAlbumCursor();
    
    abstract public int getAlbumCount();
    
    abstract public int getItemDimension();

    abstract void showClickAnimation(float x, float y);
    
    abstract void reverseClickAnimation();
    
    abstract int  getClickActionDelay();
    
    abstract void forceTextureUpdateOnNextDraw();
    
    abstract void	triggerPositionUpdate();
    
    abstract float	getPositionX();

    abstract float	getTargetPositionX();
    
    abstract float	getPositionY();
    
    abstract float	getTargetPositionY();
    
    abstract void	saveRotationInitialPosition();
    
    /** stop scrolling on touch down */
    abstract void	stopScrollOnTouch();
    
    /** scroll on touch */
    abstract void 	scrollOnTouchMove(float px, int direction);
    
    /** trigger inertial scroll on touch end */
    abstract void	inertialScrollOnTouchEnd(float scrollSpeed, int direction);
    
    /** is the cube spinning */
    abstract boolean isSpinning();
    
    /** is the cube spinning */
    abstract boolean isSpinningX();
    
    /** is the cube spinning */
    abstract boolean isSpinningY();
    
    /** get album count */
//    abstract int	getAlbumCount();
    
    /** get the current position */
    abstract int getShownPosition(float x, float y);
    
    /** get the current Album/Artist/... Id */
    abstract int getShownElementId(float x, float y);
    
    /** get the shown Album Name */
    abstract String getShownAlbumName(float x, float y);
    
    /** get the shown Album Artist Name */
    abstract String getShownAlbumArtistName(float x, float y);
    
    /** get the shown song Name */
    abstract String getShownSongName(float x, float y);
    
    /** move navigator to the specified album Id */
    abstract int setCurrentByAlbumId(long albumId);

    /** move navigator to the specified artist Id */
    abstract int setCurrentByArtistId(long artistId);

    /** move navigator to the specified audio id */
    abstract int setCurrentBySongId(long audioId);

    /** move navigator to the specified target range */
    abstract void setCurrentTargetYByProgress(float progress);
    
    /** get the scroll position [0:1] */
    abstract float getScrollPosition();

    /** get the first letter of the item in a given position [0:1] */
//    abstract char getFirstLetterInPosition(float pos);
    CharArrayBuffer oTitleCharArrayBuffer = new CharArrayBuffer(100);
    String 			oFirstLetterString = "a";	
    int				oFirstLetterPosition;
    public char getFirstLetterInPosition(float pos)
    {
    	/**
    	 * Sanity check on the cursor limits
    	 */
    	oFirstLetterPosition = Math.round(pos * mCursor.getCount());
    	if(oFirstLetterPosition >= mCursor.getCount()-1)
    		oFirstLetterPosition = mCursor.getCount()-1;
    	else if(oFirstLetterPosition < 0)
    		oFirstLetterPosition = 0;
    	mCursor.moveToPosition(oFirstLetterPosition);
    	
    	/**
    	 * Get the first letter
    	 */
    	switch(mBrowseCat)
    	{
		case Constants.BROWSECAT_ARTIST:
		    mCursor.copyStringToBuffer(
		    		mCursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.ARTIST), 
		    		oTitleCharArrayBuffer);
		    return oTitleCharArrayBuffer.data[0];
		case Constants.BROWSECAT_ALBUM:
			mCursor.copyStringToBuffer(
		    		mCursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ARTIST), 
		    		oTitleCharArrayBuffer);
		    return oTitleCharArrayBuffer.data[0];			
		case Constants.BROWSECAT_SONG:
			mCursor.copyStringToBuffer(
		    		mCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE), 
		    		oTitleCharArrayBuffer);
			/**
			 * Check if the track title begins with 'the'
			 */
			if(oTitleCharArrayBuffer.data.length >= 5)
			{
				if(oTitleCharArrayBuffer.data[0] == 't' || oTitleCharArrayBuffer.data[0] == 'T')
				{
					if(oTitleCharArrayBuffer.data[1] == 'h' || oTitleCharArrayBuffer.data[1] == 'H')
					{
						if(oTitleCharArrayBuffer.data[2] == 'e' || oTitleCharArrayBuffer.data[2] == 'E')
						{
							if(oTitleCharArrayBuffer.data[3] == ' ')
							{
								return oTitleCharArrayBuffer.data[4];
							}
						}
					}
				}
			}
			/**
			 * Check if the track title begins with 'the'
			 */
			else if(oTitleCharArrayBuffer.data.length >= 3)
			{
				if(oTitleCharArrayBuffer.data[0] == 'a' || oTitleCharArrayBuffer.data[0] == 'A')
				{
					if(oTitleCharArrayBuffer.data[1] == ' ')
					{
						return oTitleCharArrayBuffer.data[2];
					}
				}
			}
			
			return oTitleCharArrayBuffer.data[0];
    	}
    	return ' ';
    }
//    /** Scroller Element */
//    private GLScroller			mGlScroller;
    
    public 	int			mBrowseCat;
    public 	Cursor		mCursor = null;
    
    //
    public	float		mPositionX = 0.f;
    public	float		mTargetPositionX = 0.f;
    public	float		mPositionY = 0.f;
    public	float		mTargetPositionY = -1.f;
    public	float		mRotationInitialPositionY = 0.f;
    
    public	float		mViewportTop = 0.f;
    public	float		mViewportHeight = 0.f;
    
    public	boolean		mIsChangingCat = false;

	
    

}


