package com.test.sitcircular;

import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.test.sitcircular.util.Helper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

//import com.squareup.picasso.Picasso;

public class CustomAdapter extends BaseAdapter implements ListAdapter, Parcelable {
    private ArrayList<SubjectData> arrayList;
    private ArrayList<String> children;
    private Context context;
    public StorageReference temp;
    private String name;
    public CustomAdapter(Context context, ArrayList<SubjectData> arrayList, ArrayList<String> children) {
        this.arrayList=arrayList;
        this.context=context;
        this.children=children;
    }

    protected CustomAdapter(Parcel in) {
    }

    public static final Creator<CustomAdapter> CREATOR = new Creator<CustomAdapter>() {
        @Override
        public CustomAdapter createFromParcel(Parcel in) {
            return new CustomAdapter(in);
        }

        @Override
        public CustomAdapter[] newArray(int size) {
            return new CustomAdapter[size];
        }
    };

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return true;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {

    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {

    }

    @Override
    public int getCount() {
        return arrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getView(final int position,View convertView, final ViewGroup parent) {
        final SubjectData subjectData=arrayList.get(position);
        name=subjectData.SubjectName;
      if(convertView==null){
          LayoutInflater layoutInflater = LayoutInflater.from(context);
          convertView=layoutInflater.inflate(R.layout.list_row, null);
          convertView.setOnLongClickListener(new View.OnLongClickListener() {
              @Override
              public boolean onLongClick(View v) {
                  Toast.makeText(context,"Long Press",Toast.LENGTH_SHORT).show();
                  return false;
              }
          });
          final View finalConvertView = convertView;
          convertView.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                  finalConvertView.setBackgroundColor(Color.LTGRAY);
                  final SubjectData subjectData=arrayList.get(position);
                  name=subjectData.SubjectName;
                  if(subjectData.Image.equals("")){
                      subjectData.Link.child(name).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                          @Override
                          public void onSuccess(final Uri uri) {
                              String path=context.getExternalCacheDir().toString();
                              path=path.replace("Android/data/com.test.sitcircular/cache","");
                              final File f=new File(path+"/SIT/"+name);
                              if(!f.exists()) {
                                  temp=subjectData.Link.child(name);
                                  File dir = new File(Environment.getExternalStorageDirectory() + "/SIT");
                                  final File file = new File(dir, temp.getName());
                                  try {
                                      if (!dir.exists()) {
                                          dir.mkdir();
                                      }
                                      file.createNewFile();
                                  } catch (IOException e) {
                                      e.printStackTrace();
                                  }

                                  final FileDownloadTask fileDownloadTask = temp.getFile(file);
                                  Helper.initProgressDialog(context);
                                  Helper.mProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                                      @Override
                                      public void onClick(DialogInterface dialogInterface, int i) {
                                          fileDownloadTask.cancel();
                                      }
                                  });

                                  Helper.mProgressDialog.show();

                                  fileDownloadTask.addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                      @Override
                                      public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                          Helper.dismissProgressDialog();
                                          open(f,name);
                                      }
                                  }).addOnFailureListener(new OnFailureListener() {
                                      @Override
                                      public void onFailure(@NonNull Exception exception) {
                                          Helper.dismissProgressDialog();
                                      }
                                  }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                                      @Override
                                      public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                          int progress = (int) ((100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount());
                                          Helper.setProgress(progress);
                                      }
                                  });
                              }
                              else
                              {
                                  open(f,name);
                              }
                          }
                      });
                  }
                  else {
                      children.add(subjectData.SubjectName);
                      Intent intent = new Intent(context, List.class);
                      Bundle b=new Bundle();
                      b.putParcelable("link", subjectData);
                      b.putSerializable("ch",children);
                      intent.putExtra("b",b);
                      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                      context.startActivity(intent);
                  }
              }
          });
          TextView tittle=convertView.findViewById(R.id.title);
          ImageView imag=convertView.findViewById(R.id.list_image);
          tittle.setText(subjectData.SubjectName);
          if(subjectData.Image.contains("1"))
            imag.setImageResource(R.drawable.folder);
          else if(name.endsWith(".pdf"))
              imag.setImageResource(R.drawable.pdf);
          else if(name.endsWith(".docx") || name.endsWith(".doc"))
              imag.setImageResource(R.drawable.word);
          else if(name.endsWith(".xls"))
              imag.setImageResource(R.drawable.excel);
          else if(name.endsWith(".pptx") || name.endsWith(".ppt"))
              imag.setImageResource(R.drawable.ppt);
              else
          imag.setImageResource(R.drawable.file_icon);
      }
        return convertView;
    }
    public void open(File f,String name)
    {
        Intent i = new Intent(Intent.ACTION_VIEW);
        Uri u = Uri.fromFile(f);
        String mime = name.substring(name.lastIndexOf(".") + 1);
        MimeTypeMap m = MimeTypeMap.getSingleton();
        mime = m.getMimeTypeFromExtension(mime);
        i.setDataAndType(u, mime);
        i.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        context.startActivity(i);
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return arrayList.size();
    }


    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }
}
