package com.example.testapp2;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.testapp2.ml.MobilenetV110224Quant;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class MachineLearningManager {

    // TODO: Take this off the main thread; make asynchronous

    public static String AnalyzeImage(Context context, Uri imageUri) {
        String result = "";
        Bitmap originalBitmap = HelperCode.GetBitmapFromUri(context, imageUri);
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, 224, 224, true);

        try {
            MobilenetV110224Quant model = MobilenetV110224Quant.newInstance(context);

            // Creates inputs for reference.
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.UINT8);
            TensorImage tbuffer = TensorImage.fromBitmap(resizedBitmap);
            ByteBuffer byteBuffer = tbuffer.getBuffer();

            inputFeature0.loadBuffer(byteBuffer);

            // Runs model inference and gets result.
            MobilenetV110224Quant.Outputs outputs = model.process(inputFeature0);
            @NonNull TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();
            float[] outputBuffer = outputFeature0.getFloatArray();
            int maxIndex = getMaxValIndex(outputBuffer);
            String fileData = LoadData(context);
            String[] outputText = fileData.split("\n");
            Log.w("Result", outputText[maxIndex]);
            //String[] outputText = yourData.
            //String outputText = String.valueOf(labelFileArray[maxIndex]);
            result = outputText[maxIndex];

            // Releases model resources if no longer used.
            model.close();
        }
        catch (IOException e) {
            Log.e("MyError", "Got IO exception here");
            e.printStackTrace();
        }

        return result;
    }

    private static int getMaxValIndex(float[] arr){
        int index = 0;
        float max = 0.0f;
        for(int i=0; i < arr.length; i++ ){
            if(arr[i] > max ){
                max = arr[i];
                index = i;
            }
        }
        return index;
    }

    // reading labels.txt (annotations)
    private static String LoadData(Context context) {
        String tContents = "";

        try {
            InputStream stream = context.getAssets().open("labels.txt");

            int size = stream.available();
            byte[] buffer = new byte[size];
            stream.read(buffer);
            stream.close();
            tContents = new String(buffer);

        } catch (IOException e) {
            // Handle exceptions here
        }

        return tContents;

    }
}
