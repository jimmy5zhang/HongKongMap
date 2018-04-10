package com.hteng.hongkong.map;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by jimmyzhang on 2018/4/10.
 * 自定义地图View
 */

public class MapView extends View {
    private Context context;
    private List<ProviceItem> itemList;
    private float scale = 1.3f;
    private int[] colorArray = new int[]{0xFF239BD7, 0xFF30A9E5, 0xFF80CBF1, 0xFFFFFFFF};
    private Paint paint;
    private ProviceItem selectItem;
    private GestureDetectorCompat gestureDetectorCompat;

    public MapView(Context context) {
        this(context, null);

    }

    public MapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        paint = new Paint();
        paint.setAntiAlias(true);
        loadThread.start();
        gestureDetectorCompat = new GestureDetectorCompat(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                handleTouch(e.getX(), e.getY());
                return true;
            }
        });
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (itemList != null) {
            canvas.save();
            canvas.scale(scale, scale);
            for (ProviceItem item : itemList) {
                if (item != selectItem) {
                    item.drawItem(canvas, paint, false);
                }
            }
            if (selectItem != null) {
                selectItem.drawItem(canvas, paint, true);
            }
        }
    }

    private void handleTouch(float x, float y) {
        if (itemList == null) {
            return;
        }
        ProviceItem tmpItem = null;
        for (ProviceItem item : itemList) {
            if (item.isTouch((int) (x / scale), (int) (y / scale))) {
                tmpItem = item;
                break;
            }
        }
        if (tmpItem != null) {
            selectItem = tmpItem;
            postInvalidate();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        handleTouch(event.getX(), event.getY());
        return true;
    }

    private Thread loadThread = new Thread() {
        @Override
        public void run() {
            List<ProviceItem> list = new ArrayList<>();
            InputStream inputStream = context.getResources().openRawResource(R.raw.hong_kong_map);
            try {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();  //取得DocumentBuilderFactory实例
                DocumentBuilder builder = factory.newDocumentBuilder(); //从factory获取DocumentBuilder实例
                Document doc = builder.parse(inputStream);   //解析输入流 得到Document实例
                Element rootElement = doc.getDocumentElement();
                NodeList items = rootElement.getElementsByTagName("path");
                for (int i = 0; i < items.getLength(); i++) {
                    Element element = (Element) items.item(i);
                    String pathData = element.getAttribute("android:pathData");
                    Log.i("tuch", "pathData   " + pathData);
                    Path path = PathParser.createPathFromPathData(pathData);
                    ProviceItem proviceItem = new ProviceItem(path);
                    list.add(proviceItem);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            itemList = list;
            handler.sendEmptyMessage(1);
        }
    };
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (itemList == null) {
                return;
            }
            int totalNumber = itemList.size();
            for (int i = 0; i < totalNumber; i++) {
                int color = Color.WHITE;
                int flag = i % 4;
                switch (flag) {
                    case 1:
                        color = colorArray[0];
                        break;
                    case 2:
                        color = colorArray[1];
                        break;
                    case 3:
                        color = colorArray[2];
                        break;
                    default:
                        color = Color.WHITE;
                        break;
                }
                itemList.get(i).setDrawColor(color);
            }
            postInvalidate();
        }
    };

}
