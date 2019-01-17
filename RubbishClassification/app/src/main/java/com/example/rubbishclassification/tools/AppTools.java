package com.example.rubbishclassification.tools;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.example.rubbishclassification.MyApplication;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AppTools {
    private static final char HEX_DIGITS[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'a', 'b', 'c', 'd', 'e', 'f'};

    private static String convertToHexString(byte[] b) {
        StringBuilder sb = new StringBuilder(b.length * 2);
        for (byte a : b) {
            sb.append(HEX_DIGITS[(a & 0xf0) >>> 4]);
            sb.append(HEX_DIGITS[a & 0x0f]);
        }
        return sb.toString();
    }

    /**
     * Get a String's HashCode
     *
     * @param str String
     * @return HashCode
     */
    public static String getMD5String(String str) {
        MessageDigest md5;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
        md5.update(str.getBytes());
        return convertToHexString(md5.digest());
    }

    /**
     * Get a File's HashCode
     *
     * @param file File
     * @return HashCode
     */
    public static String getMD5String(File file) {
        // Create md5
        MessageDigest md5;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
        // Stream
        InputStream in = null;
        byte[] buffer = new byte[1024];
        int numRead;
        // Read
        try {
            in = new FileInputStream(file);
            while ((numRead = in.read(buffer)) > 0) {
                md5.update(buffer, 0, numRead);
            }
            return convertToHexString(md5.digest());
        } catch (Exception e) {
            return null;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 质量压缩法
     *
     * @param image
     * @return
     */
    public static byte[] compressImage(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;
        while (baos.toByteArray().length / 1024 > 100) {  //循环判断如果压缩后图片是否大于100kb,大于继续压缩
            baos.reset();//重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中
            options -= 10;//每次都减少10
            if (options <= 10) {
                break;
            }
        }
        byte[] byteArray = baos.toByteArray();
        try {
            if (baos != null) {
                baos.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return byteArray;
    }

    /**
     * 检查网络是否连通
     *
     * @return boolean
     */
    public static boolean isNetworkAvailable(Context context) {
        // 创建并初始化连接对象
        ConnectivityManager connMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        // 判断初始化是否成功并作出相应处理
        if (connMan != null) {
            // 调用getActiveNetworkInfo方法创建对象,如果不为空则表明网络连通，否则没连通
            NetworkInfo info = connMan.getActiveNetworkInfo();
            if (info != null) {
                return info.isAvailable();
            }
        }
        return false;
    }

    /**
     * Toast_Long
     */
    public static void toastLong(String toast){
        Toast.makeText(MyApplication.getContext(),toast,Toast.LENGTH_LONG).show();
    }

    /**
     * Toast_Short
     */
    public static void toastShort(String toast){
        Toast.makeText(MyApplication.getContext(),toast,Toast.LENGTH_SHORT).show();
    }

    /**
     * 判断相机权限
     * */
    public static boolean cameraIsCanUse() {
        boolean isCanUse = true;
        Camera mCamera = null;
        try {
            mCamera = Camera.open();
            Camera.Parameters mParameters = mCamera.getParameters(); //针对魅族手机
            mCamera.setParameters(mParameters);
        } catch (Exception e) {
            isCanUse = false;
        }

        if (mCamera != null) {
            try {
                mCamera.release();
            } catch (Exception e) {
                e.printStackTrace();
                return isCanUse;
            }
        }
        return isCanUse;
    }



    public static String compressUpImage(String filePath) {
        if (TextUtils.isEmpty(filePath)) return null;
        //旋转角度
        int degree = readPictureDegree(filePath);
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPurgeable = true;// 同时设置才会有效
        opt.inInputShareable = true;//。当系统内存不够时候图片自动被回收
        opt.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, opt);
        opt.inSampleSize = calculateInSampleSize(opt, 1028, 1028);
        opt.inJustDecodeBounds = false;
        Bitmap cbitmap = BitmapFactory.decodeFile(filePath, opt);
        Log.i("==image==","压缩前的尺寸：  宽： " + opt.outWidth + " ----  高：" + opt.outHeight);
        Bitmap image = null;
        Bitmap upImage = null;
        int quality = 100;
        if (Math.abs(degree) > 0) {
            try {
                image = rotaingImage(degree, cbitmap);
            } catch (Exception e) {
                e.printStackTrace();
                image = cbitmap;
            }
        } else {
            image = cbitmap;
        }
        if(image==null) return "";
        int width = image.getWidth();
        int height = image.getHeight();
        File tempFile = null;
        if (width <= 1028 && height <= 1028) {
            upImage = image;
            if(upImage!=null&&upImage.getByteCount()>102400) {
                quality = 60;
            }
            Log.i("==image==","图片宽或者高均小于或等于1280时图片尺寸保持不变，但仍然经过图片压缩处理，得到小文件的同尺寸图片");
        } else if ((width > 1028 || height > 1028) && width / height <= 2) {
            Log.i("==image==","宽或者高大于1280，但是图片宽度高度比小于或等于2，则将图片宽或者高取大的等比压缩至1280");
            int newWidth = 1028;
            int newHeight = 1028;
            if (width <= height) {
                newWidth = (int) ((width * 1028f) / height);
            } else {
                newHeight = (int) ((height * 1028f) / width);
            }
            upImage = Bitmap.createScaledBitmap(image, newWidth, newHeight, false);
            if (image != null && !image.isRecycled()) {
                image.recycle();
            }
            quality = 60;
        } else if (width > 1028 && height > 1028 && width / height > 2) {
            Log.i("==image==","宽或者高大于1280，但是图片宽高比大于2时，并且宽以及高均大于1280，则宽或者高取小的等比压缩至1280");
            int newWidth = 1028;
            int newHeight = 1028;
            if (width >= height) {
                newWidth = (int) ((width * 1028f) / height);
            } else {
                newHeight = (int) ((height * 1028f) / width);
            }
            upImage = Bitmap.createScaledBitmap(image, newWidth, newHeight, false);
            if (image != null && !image.isRecycled()) {
                image.recycle();
            }
            quality = 60;
        } else if ((width > 1028 && height < 1028) || (width < 1028 && height > 1028) && width / height > 2) {
            Log.i("==image==","宽或者高大于1280，但是图片宽高比大于2时，并且宽或者高其中一个小于1280，则压缩至同尺寸的小文件图片");
            upImage = image;
            quality = 60;
        } else {
            upImage = image;
            quality = 60;
        }
        Log.i("==image==","压缩后的尺寸：  宽：" + upImage.getWidth() + " ---  高： " + upImage.getHeight());
        FileOutputStream out = null;
        try {
            //创建临时文件
            tempFile = File.createTempFile("upImage", ".jpg");
            out = new FileOutputStream(tempFile);
            upImage.compress(Bitmap.CompressFormat.JPEG, quality, out);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (out != null) {
                try {
                    out.flush();
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (tempFile == null) {
                return null;
            } else {
                return tempFile.getAbsolutePath();
            }
        }
    }

    /*
     * 旋转图片
     * @param angle
     * @param bitmap
     * @return Bitmap
     */
    public static Bitmap rotaingImage(int angle, Bitmap bitmap) {
        //旋转图片 动作
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        Log.i("==image==","angle=" + angle);
        // 创建新的图片
        Bitmap returnBm = null;
        try {
            // 将原始图片按照旋转矩阵进行旋转，并得到新的图片
            returnBm = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            if (returnBm == null) {
                returnBm = bitmap;
            }
            if (bitmap != returnBm) {
                bitmap.recycle();
            }
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }
        return returnBm;
    }

    /**
     * 读取图片属性：旋转的角度
     *
     * @param path 图片绝对路径
     * @return degree旋转的角度
     */
    public static int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    /**
     *计算图片的缩放值
     */
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }

}
