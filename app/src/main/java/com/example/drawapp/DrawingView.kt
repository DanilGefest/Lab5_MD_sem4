package com.example.drawapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration

class DrawingView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private var mDrawPath: CustomPath = CustomPath()

    private var mCanvasBitmap: Bitmap? = null        // побитовая разметка области
    private lateinit var mDrawPaint: Paint          // хранит характеристики рисунка
    private lateinit var mCanvasPaint: Paint        // хранит характеристики области
    private var mBrushSize: Float = 0F              // размер маркера
    private lateinit var canvas: Canvas             // инциализация графической области рисования

    private var mBackgoundImage: Bitmap? = null

    private val mPath = arrayOf<CustomPath>()

    private val touchTol = ViewConfiguration.get(context).scaledEdgeSlop
    private var motionX = 0f
    private var motionY = 0f
    private var currentX = 0f
    private var currentY = 0f

    init {
        setUpDrawing()
    }

    fun setUpDrawing() {
        mDrawPath.color = CustomPath().color
        mDrawPath.brushThickness = CustomPath().brushThickness
        mDrawPaint = Paint()
        mDrawPaint.color = mDrawPath.color
        mDrawPaint.style = Paint.Style.STROKE
        mDrawPaint.strokeJoin = Paint.Join.ROUND
        mDrawPaint.strokeCap = Paint.Cap.ROUND
        mCanvasPaint = Paint()
        mCanvasPaint.flags = Paint.DITHER_FLAG
        mBrushSize = 20f
    }

    fun changeColor(color: Int){
        mDrawPath.color = color
        check()
    }

    fun changeThickness(thic: Float){
        mDrawPath.brushThickness = thic
        check()
    }

    fun loadBackground(bitmap: Bitmap) {
        mBackgoundImage = bitmap
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        invalidate()
    }

    fun getBitMap(): Bitmap {
        val returnedBitmap = Bitmap.createBitmap(this.width, this.height, Bitmap.Config.ARGB_8888) //для того чтобы получить битмап drawing view (битмап - побитовая картинка)
        val c = Canvas(returnedBitmap)
        val bgDrawable = this.background
        if (bgDrawable != null) {
            bgDrawable.draw(c)
        } else {
            c.drawColor(Color.WHITE)
        }
        this.draw(c)
        return returnedBitmap
    }

    internal class CustomPath(): android.graphics.Path() {
        var color: Int = Color.BLACK  //цвет маркера
        var brushThickness = 20f
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mCanvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        canvas = Canvas(mCanvasBitmap!!)
    }

    fun check(){
        if(mDrawPath.isEmpty){
            mDrawPaint.color = mDrawPath.color
            mDrawPaint.strokeWidth = mDrawPath.brushThickness
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        mCanvasPaint.color = Color.GRAY

        if(mBackgoundImage != null){
            canvas.drawBitmap(mBackgoundImage!!, 0f, 0f, mCanvasPaint)
        }
        canvas.drawBitmap(mCanvasBitmap!!, 0F, 0F, mCanvasPaint)
        check()

        mPath.forEach {
            mDrawPaint.color = it.color
            mDrawPaint.strokeWidth = it.brushThickness
            canvas.drawPath(it, mDrawPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        motionX = event!!.x
        motionY = event.y

        if(event.action == MotionEvent.ACTION_DOWN){
            mDrawPath.reset()
            mDrawPath.moveTo(motionX, motionY)
            mDrawPath.lineTo(motionX, motionY)
            canvas.drawPath(mDrawPath, mDrawPaint)
            currentX = motionX
            currentY = motionY
        } else if(event.action == MotionEvent.ACTION_MOVE){
            val dx = Math.abs(motionX-currentX)
            val dy = Math.abs(motionY-currentY)

            if(dx>=touchTol || dy>= touchTol){             //quad to рисует более плавные линии, прикеольнее

                mDrawPath.quadTo(currentX, currentY, (motionX+currentX)/2, (motionY+currentY)/2)
                currentX = motionX
                currentY = motionY
                canvas.drawPath(mDrawPath, mDrawPaint)
            }
        } else if(event.action == MotionEvent.ACTION_UP){
            mDrawPath.reset()
            mPath.plus(mDrawPath)
        } else {
            return false
        }
        invalidate()
        return true
    }
}
