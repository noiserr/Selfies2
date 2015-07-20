package pl.droidsonroids.letmetakeaselfie.ui;

import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pl.droidsonroids.letmetakeaselfie.R;
import pl.droidsonroids.letmetakeaselfie.api.SelfieApiManager;
import pl.droidsonroids.letmetakeaselfie.api.response.GetSelfiesResponse;
import pl.droidsonroids.letmetakeaselfie.api.response.PostSelfieResponse;
import pl.droidsonroids.letmetakeaselfie.model.Selfie;
import pl.droidsonroids.letmetakeaselfie.ui.adapter.SelfieAdapter;
import pl.droidsonroids.letmetakeaselfie.ui.dialog.UserNameDialogFragment;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MainActivity extends AppCompatActivity implements UserNameDialogFragment.OnUserNameTypedListener {

    @Bind(R.id.refresh_layout) SwipeRefreshLayout swipeRefreshLayout;
    @Bind(R.id.recycler_view) RecyclerView recyclerView;

    private SelfieAdapter selfieAdapter;
    private String mCurrentPhotoPath;
    private File mPhotoFile = null;
    static final int REQUEST_TAKE_PHOTO = 1;
    private boolean mResultOk = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
        initSelfieAdapter();
        setupSwipeRefreshLayout();
        setupRecyclerView();
        postRefresh();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mResultOk){
                new UserNameDialogFragment().show(getSupportFragmentManager(), null);

        }
    }

    private void initSelfieAdapter() {
        selfieAdapter = new SelfieAdapter();
    }

    private void setupSwipeRefreshLayout() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshSelfies();
            }
        });
    }

    private void setupRecyclerView() {
        recyclerView.setAdapter(selfieAdapter);
        recyclerView.setLayoutManager(new GridLayoutManager(this, getResources().getInteger(R.integer.grid_span_count)));
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(final Rect outRect, final View view, final RecyclerView parent, final RecyclerView.State state) {
                int offset = getResources().getDimensionPixelSize(R.dimen.grid_padding);
                outRect.set(offset, offset, offset, offset);
            }
        });
    }

    private void postRefresh() {
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
                refreshSelfies();
            }
        });
    }

    private void refreshSelfies() {
        SelfieApiManager.getInstance().getSelfies(new Callback<GetSelfiesResponse>() {
            @Override
            public void success(final GetSelfiesResponse getSelfiesResponse, final Response response) {
                swipeRefreshLayout.setRefreshing(false);
                selfieAdapter.setSelfies(getSelfiesResponse.getSelfies());
            }

            @Override
            public void failure(final RetrofitError error) {
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(MainActivity.this, R.string.selfies_load_error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        mResultOk = true;

    }

    @OnClick(R.id.button_add)
    public void addSelfie() {
        dispatchTakePictureIntent();
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            try {
                mPhotoFile = createImageFile();
            } catch (IOException ex) {
            }
            if (mPhotoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(mPhotoFile));
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }



    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }

    @Override
    public void onUserNameTyped(@NonNull final String userName) {
        Selfie selfie = new Selfie();
        selfie.setUserName(userName);
        if (mPhotoFile != null) {
            selfie.setPhotoFile(mPhotoFile); // todo: put photo here
        }else {
            Toast.makeText(getApplication(), "Cant send file", Toast.LENGTH_SHORT).show();
        }

        SelfieApiManager.getInstance().postSelfie(selfie, new Callback<PostSelfieResponse>() {
            @Override
            public void success(final PostSelfieResponse postSelfieResponse, final Response response) {
                refreshSelfies();
            }
            @Override
            public void failure(final RetrofitError error) {
                Toast.makeText(MainActivity.this, R.string.selfie_upload_error, Toast.LENGTH_SHORT).show();
            }
        });
    }


}
