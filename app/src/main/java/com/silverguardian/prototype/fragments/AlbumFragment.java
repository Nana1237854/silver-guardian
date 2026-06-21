package com.silverguardian.prototype.fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.silverguardian.prototype.PhotoDetailActivity;
import com.silverguardian.prototype.R;
import com.silverguardian.prototype.album.PhotoStorage;
import com.silverguardian.prototype.models.Album;
import com.silverguardian.prototype.models.AlbumPhoto;
import com.silverguardian.prototype.utils.FormFieldFactory;
import com.silverguardian.prototype.utils.GalleryPermissionHelper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AlbumFragment extends BaseFragment {
    private static final int REQUEST_CAMERA = 702;

    private GalleryPermissionHelper permHelper;
    private RecyclerView root;
    private Uri pendingImageUri;
    private Uri cameraImageUri;
    private ImageView pendingPreview;
    private TextView pendingPickChip;
    private Album currentAlbum;
    private boolean showingAllPhotos;
    private String photoQuery = "";
    private boolean newestFirst = true;

    private final List<Album> albums = new ArrayList<>();
    private final List<AlbumPhoto> allPhotos = new ArrayList<>();
    private final List<AlbumPhoto> albumPhotos = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root = (RecyclerView) inflater.inflate(R.layout.fragment_album, container, false);
        permHelper = new GalleryPermissionHelper(this);
        refreshData();

        String initialAlbumName = getArguments() == null ? null : getArguments().getString("album_name");
        if (initialAlbumName != null && !initialAlbumName.trim().isEmpty()) {
            Album initialAlbum = findAlbumByName(initialAlbumName.trim());
            if (initialAlbum != null) {
                buildPhotoGrid(initialAlbum, false);
            } else {
                buildAlbumList();
            }
        } else {
            buildAlbumList();
        }
        return root;
    }

    private void refreshData() {
        albums.clear();
        albums.addAll(familyAlbum().getAlbums());
        allPhotos.clear();
        allPhotos.addAll(familyAlbum().getPhotos());
    }

    private void transitionTo(Runnable buildScreen) {
        AlphaAnimation fade = new AlphaAnimation(0.35f, 1.0f);
        fade.setDuration(160);
        buildScreen.run();
        root.startAnimation(fade);
    }

    private void buildAlbumList() {
        currentAlbum = null;
        showingAllPhotos = false;
        photoQuery = "";
        refreshData();

        GridLayoutManager manager = new GridLayoutManager(requireContext(), 2);
        AlbumListAdapter adapter = new AlbumListAdapter();
        manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return adapter.getItemViewType(position) == AlbumListAdapter.TYPE_ALBUM ? 1 : 2;
            }
        });
        root.setLayoutManager(manager);
        root.setAdapter(adapter);
    }

    private void buildPhotoGrid(@Nullable Album album, boolean allPhotosMode) {
        currentAlbum = album;
        showingAllPhotos = allPhotosMode;
        refreshData();
        albumPhotos.clear();
        if (allPhotosMode) {
            albumPhotos.addAll(filterPhotos(new ArrayList<>(allPhotos)));
            if (!newestFirst) {
                Collections.reverse(albumPhotos);
            }
        } else if (album != null) {
            albumPhotos.addAll(familyAlbum().getPhotosForAlbum(album, photoQuery, newestFirst));
        }

        GridLayoutManager manager = new GridLayoutManager(requireContext(), 3);
        PhotoGridAdapter adapter = new PhotoGridAdapter();
        manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return adapter.getItemViewType(position) == PhotoGridAdapter.TYPE_PHOTO ? 1 : 3;
            }
        });
        root.setLayoutManager(manager);
        root.setAdapter(adapter);
    }    private List<AlbumPhoto> filterPhotos(List<AlbumPhoto> source) {
        if (photoQuery == null || photoQuery.isEmpty()) {
            return source;
        }
        List<AlbumPhoto> filtered = new ArrayList<>();
        for (AlbumPhoto photo : source) {
            boolean matches = (photo.title != null && photo.title.contains(photoQuery))
                || (photo.familyMessage != null && photo.familyMessage.contains(photoQuery))
                || (photo.category != null && photo.category.contains(photoQuery));
            if (matches) {
                filtered.add(photo);
            }
        }
        return filtered;
    }

    private Album findAlbumByName(String name) {
        for (Album album : albums) {
            if (name.equals(album.name)) {
                return album;
            }
        }
        return null;
    }

    private String currentAlbumTitle() {
        return showingAllPhotos ? getString(R.string.album_all_photos)
            : currentAlbum == null ? getString(R.string.album_title) : currentAlbum.name;
    }

    private void showPhotoSearch() {
        LinearLayout form = (LinearLayout) LayoutInflater.from(requireContext())
            .inflate(R.layout.view_dialog_form_container, null, false);
        EditText input = FormFieldFactory.addTextField(requireContext(), form,
            getString(R.string.album_search_title), getString(R.string.album_search_hint), false);
        new AlertDialog.Builder(requireContext())
            .setTitle(R.string.album_search_title)
            .setView(form)
            .setPositiveButton(R.string.album_search, (dialog, which) -> {
                photoQuery = input.getText().toString().trim();
                buildPhotoGrid(currentAlbum, showingAllPhotos);
            })
            .setNegativeButton(R.string.common_cancel, null)
            .show();
    }

    private void askCreateAlbum() {
        LinearLayout form = (LinearLayout) LayoutInflater.from(requireContext())
            .inflate(R.layout.view_dialog_form_container, null, false);
        EditText input = FormFieldFactory.addTextField(requireContext(), form,
            getString(R.string.album_name), getString(R.string.album_name_hint), false);
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
            .setTitle(R.string.album_create_new)
            .setView(form)
            .setPositiveButton(R.string.album_create, null)
            .setNegativeButton(R.string.common_cancel, null)
            .create();
        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = input.getText().toString().trim();
            if (name.isEmpty()) {
                input.setError(getString(R.string.album_name_error));
                input.requestFocus();
                return;
            }
            Album album = familyAlbum().createAlbum(name);
            dialog.dismiss();
            transitionTo(() -> buildPhotoGrid(album, false));
        }));
        dialog.show();
    }

    private void askDeleteAlbum(Album album) {
        new AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.album_delete_title, album.name))
            .setMessage(R.string.album_delete_confirm)
            .setPositiveButton(R.string.common_delete, (dialog, which) -> {
                familyAlbum().deleteAlbum(album);
                transitionTo(this::buildAlbumList);
            })
            .setNegativeButton(R.string.common_cancel, null)
            .show();
    }

    private void askUploadPhoto() {
        if (showingAllPhotos || currentAlbum == null) {
            toast(getString(R.string.album_manage_hint));
            return;
        }

        pendingImageUri = null;
        View form = LayoutInflater.from(requireContext()).inflate(R.layout.view_album_upload_form, null, false);
        pendingPreview = form.findViewById(R.id.album_upload_preview);
        pendingPickChip = form.findViewById(R.id.album_upload_pick_chip);
        LinearLayout fieldsContainer = form.findViewById(R.id.album_upload_fields_container);

        pendingPreview.setClipToOutline(true);
        pendingPreview.setBackgroundTintList(ColorStateList.valueOf(color(R.color.surface_mint)));
        pendingPreview.setImageResource(R.drawable.ic_album);

        EditText titleInput = FormFieldFactory.addTextField(requireContext(), fieldsContainer,
            getString(R.string.album_photo_title), getString(R.string.album_name_hint), false);
        EditText messageInput = FormFieldFactory.addTextField(requireContext(), fieldsContainer,
            getString(R.string.album_message_label), getString(R.string.album_message_hint), true);
        pendingPickChip.setOnClickListener(v -> chooseImageSource());

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.album_upload_to, currentAlbum.name))
            .setView(form)
            .setPositiveButton(R.string.common_save, null)
            .setNegativeButton(R.string.common_cancel, null)
            .create();
        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            if (pendingImageUri == null) {
                pendingPickChip.setError(getString(R.string.album_pick_error));
                return;
            }
            String title = titleInput.getText().toString().trim();
            if (title.isEmpty()) {
                title = getString(R.string.album_new_photo);
            }
            try {
                PhotoStorage.SavedPhoto saved = PhotoStorage.save(requireContext(), pendingImageUri);
                familyAlbum().addPhoto(currentAlbum, saved.privatePath, saved.publicUri, title,
                    messageInput.getText().toString().trim());
                Toast.makeText(requireContext(), R.string.album_upload_success, Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                transitionTo(() -> buildPhotoGrid(currentAlbum, false));
            } catch (IOException e) {
                Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }));
        dialog.show();
    }

    private void chooseImageSource() {
        new AlertDialog.Builder(requireContext())
            .setTitle("Select image source")
            .setItems(new String[]{"Camera", "Gallery"}, (dialog, which) -> {
                if (which == 0) {
                    openCamera();
                } else {
                    permHelper.requestPermissionThen(permHelper::openGallery);
                }
            })
            .show();
    }

    private void openCamera() {
        try {
            File dir = new File(requireContext().getCacheDir(), "camera");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File file = File.createTempFile("album_", ".jpg", dir);
            cameraImageUri = androidx.core.content.FileProvider.getUriForFile(
                requireContext(), requireContext().getPackageName() + ".files", file);
            Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, cameraImageUri);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(intent, REQUEST_CAMERA);
        } catch (IOException e) {
            Toast.makeText(requireContext(), "Unable to open camera", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri image = requestCode == REQUEST_CAMERA && resultCode == Activity.RESULT_OK
            ? cameraImageUri
            : GalleryPermissionHelper.handleActivityResult(requestCode, resultCode, data);
        if (image == null) {
            return;
        }
        pendingImageUri = image;
        if (pendingPreview != null) {
            pendingPreview.setImageURI(image);
            pendingPreview.setBackgroundColor(0x00000000);
        }
        if (pendingPickChip != null) {
            pendingPickChip.setText(R.string.album_picked);
            pendingPickChip.setTextColor(color(R.color.primary));
            pendingPickChip.setError(null);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (!permHelper.onRequestPermissionsResult(requestCode, grantResults)) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
    private class AlbumListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        static final int TYPE_HEADER = 0;
        static final int TYPE_ALBUM = 1;
        static final int TYPE_FOOTER = 2;

        @Override
        public int getItemViewType(int position) {
            if (position == 0) {
                return TYPE_HEADER;
            }
            if (position == getItemCount() - 1) {
                return TYPE_FOOTER;
            }
            return TYPE_ALBUM;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            if (viewType == TYPE_HEADER) {
                return new AlbumListHeaderHolder(inflater.inflate(R.layout.item_album_list_header, parent, false));
            }
            if (viewType == TYPE_FOOTER) {
                return new AlbumCreateFooterHolder(inflater.inflate(R.layout.item_album_create_footer, parent, false));
            }
            return new AlbumHolder(inflater.inflate(R.layout.item_album, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof AlbumListHeaderHolder) {
                ((AlbumListHeaderHolder) holder).bind();
            } else if (holder instanceof AlbumCreateFooterHolder) {
                ((AlbumCreateFooterHolder) holder).bind();
            } else if (holder instanceof AlbumHolder) {
                ((AlbumHolder) holder).bind(albums.get(position - 1));
            }
        }

        @Override
        public int getItemCount() {
            return albums.size() + 2;
        }
    }

    private class AlbumListHeaderHolder extends RecyclerView.ViewHolder {
        AlbumListHeaderHolder(View itemView) {
            super(itemView);
        }

        void bind() {
            View profile = itemView.findViewById(R.id.album_profile_header);
            ((TextView) profile.findViewById(R.id.header_title)).setText(R.string.album_title);
            ((TextView) profile.findViewById(R.id.header_subtitle)).setText(R.string.album_subtitle);
            ImageView avatar = profile.findViewById(R.id.header_avatar);
            avatar.setImageResource(R.drawable.elder_profile);
            avatar.setClipToOutline(true);

            View hero = itemView.findViewById(R.id.album_family_banner);
            ((ImageView) hero.findViewById(R.id.hero_image)).setImageResource(R.drawable.family_companion);
            ((TextView) hero.findViewById(R.id.hero_title)).setText(R.string.album_family_always);
            ((TextView) hero.findViewById(R.id.hero_subtitle)).setText(R.string.album_updated_today);

            View all = itemView.findViewById(R.id.album_all_photos_entry);
            ((TextView) all.findViewById(R.id.all_photos_title)).setText(R.string.album_all_photos);
            ((TextView) all.findViewById(R.id.all_photos_count)).setText(
                getString(R.string.album_photo_count_summary, allPhotos.size()));
            ImageView cover = all.findViewById(R.id.all_photos_cover);
            if (allPhotos.isEmpty()) {
                cover.setImageResource(R.drawable.family_companion);
            } else {
                loadPhotoImage(cover, allPhotos.get(0));
            }
            all.setOnClickListener(v -> transitionTo(() -> buildPhotoGrid(null, true)));

            View section = itemView.findViewById(R.id.album_list_section_header);
            ((TextView) section.findViewById(R.id.album_section_title)).setText(R.string.album_my_albums);
            ((TextView) section.findViewById(R.id.album_section_count)).setText(
                getString(R.string.album_unit_count, albums.size()));
        }
    }

    private class AlbumCreateFooterHolder extends RecyclerView.ViewHolder {
        AlbumCreateFooterHolder(View itemView) {
            super(itemView);
        }

        void bind() {
            TextView create = itemView.findViewById(R.id.album_create_footer_action);
            create.setText(R.string.album_create_new);
            create.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_add, 0, 0, 0);
            create.setOnClickListener(v -> askCreateAlbum());
        }
    }

    private class AlbumHolder extends RecyclerView.ViewHolder {
        private final TextView title;
        private final TextView count;
        private final ImageView image;

        AlbumHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.photo_title);
            count = itemView.findViewById(R.id.photo_category);
            itemView.findViewById(R.id.photo_message).setVisibility(View.GONE);
            itemView.findViewById(R.id.photo_favorite).setVisibility(View.GONE);
            image = itemView.findViewById(R.id.photo_image);
        }

        void bind(Album album) {
            title.setText(album.name);
            count.setText(getString(R.string.album_photo_count_summary, album.photoCount));
            loadAlbumCover(image, album);
            itemView.setOnClickListener(v -> transitionTo(() -> buildPhotoGrid(album, false)));
            itemView.setOnLongClickListener(v -> {
                askDeleteAlbum(album);
                return true;
            });
        }
    }

    private class PhotoGridAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        static final int TYPE_HEADER = 0;
        static final int TYPE_PHOTO = 1;
        static final int TYPE_EMPTY = 2;
        static final int TYPE_FOOTER = 3;

        @Override
        public int getItemViewType(int position) {
            if (position == 0) {
                return TYPE_HEADER;
            }
            if (position == getItemCount() - 1) {
                return TYPE_FOOTER;
            }
            return albumPhotos.isEmpty() ? TYPE_EMPTY : TYPE_PHOTO;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            if (viewType == TYPE_HEADER) {
                return new PhotoHeaderHolder(inflater.inflate(R.layout.item_album_detail_header, parent, false));
            }
            if (viewType == TYPE_FOOTER) {
                return new PhotoFooterHolder(inflater.inflate(R.layout.item_album_photo_footer, parent, false));
            }
            if (viewType == TYPE_EMPTY) {
                return new EmptyAlbumHolder(inflater.inflate(R.layout.view_album_empty_state, parent, false));
            }
            return new PhotoHolder(inflater.inflate(R.layout.item_photo_grid, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof PhotoHeaderHolder) {
                ((PhotoHeaderHolder) holder).bind();
            } else if (holder instanceof PhotoFooterHolder) {
                ((PhotoFooterHolder) holder).bind();
            } else if (holder instanceof EmptyAlbumHolder) {
                ((EmptyAlbumHolder) holder).bind();
            } else if (holder instanceof PhotoHolder) {
                ((PhotoHolder) holder).bind(albumPhotos.get(position - 1));
            }
        }

        @Override
        public int getItemCount() {
            return 2 + (albumPhotos.isEmpty() ? 1 : albumPhotos.size());
        }
    }

    private class PhotoHeaderHolder extends RecyclerView.ViewHolder {
        PhotoHeaderHolder(View itemView) {
            super(itemView);
        }

        void bind() {
            View header = itemView.findViewById(R.id.album_detail_page_header);
            TextView back = header.findViewById(R.id.header_back);
            back.setVisibility(View.VISIBLE);
            back.setText(R.string.common_back);
            back.setOnClickListener(v -> transitionTo(AlbumFragment.this::buildAlbumList));
            ((TextView) header.findViewById(R.id.header_title)).setText(currentAlbumTitle());

            TextView more = header.findViewById(R.id.header_action);
            if (showingAllPhotos || currentAlbum == null) {
                more.setVisibility(View.GONE);
            } else {
                more.setVisibility(View.VISIBLE);
                more.setText(R.string.common_delete);
                more.setOnClickListener(v -> askDeleteAlbum(currentAlbum));
            }

            View hero = itemView.findViewById(R.id.album_detail_hero);
            ImageView heroImage = hero.findViewById(R.id.hero_image);
            if (albumPhotos.isEmpty()) {
                if (showingAllPhotos) {
                    heroImage.setImageResource(R.drawable.family_companion);
                } else {
                    loadAlbumCover(heroImage, currentAlbum);
                }
            } else {
                loadPhotoImage(heroImage, albumPhotos.get(0));
            }
            ((TextView) hero.findViewById(R.id.hero_title)).setText(currentAlbumTitle());
            ((TextView) hero.findViewById(R.id.hero_subtitle)).setText(
                getString(R.string.album_hero_subtitle, albumPhotos.size()));

            View memory = itemView.findViewById(R.id.album_detail_memory);
            ((TextView) memory.findViewById(R.id.card_title)).setText(R.string.album_memory_note);
            ((TextView) memory.findViewById(R.id.card_body)).setText(R.string.album_memory_body);
            LinearLayout actions = memory.findViewById(R.id.card_actions);
            actions.removeAllViews();
            actions.addView(memoryAction(actions, getString(R.string.album_favorite), v -> toggleFirstFavorite()));
            actions.addView(memoryAction(actions, getString(R.string.album_share), v -> toast(getString(R.string.album_share_opened))));
            actions.addView(memoryAction(actions, getString(R.string.album_voice_memory), v -> toast(getString(R.string.album_voice_hint))));

            View section = itemView.findViewById(R.id.album_detail_section_header);
            ((TextView) section.findViewById(R.id.album_section_title)).setText(R.string.album_all_photos);
            ((TextView) section.findViewById(R.id.album_section_count)).setText(
                getString(R.string.album_photo_count_summary, albumPhotos.size()));
        }
    }
    private class EmptyAlbumHolder extends RecyclerView.ViewHolder {
        EmptyAlbumHolder(View itemView) {
            super(itemView);
        }

        void bind() {
            ((TextView) itemView.findViewById(R.id.album_empty_title)).setText(R.string.album_empty_title);
            ((TextView) itemView.findViewById(R.id.album_empty_subtitle)).setText(R.string.album_empty_subtitle);
            TextView action = itemView.findViewById(R.id.album_empty_action);
            action.setText(R.string.album_upload_first);
            action.setOnClickListener(v -> askUploadPhoto());
        }
    }

    private class PhotoFooterHolder extends RecyclerView.ViewHolder {
        PhotoFooterHolder(View itemView) {
            super(itemView);
        }

        void bind() {
            View row = itemView.findViewById(R.id.album_footer_action_row);
            TextView sort = row.findViewById(R.id.action_left);
            sort.setText(R.string.album_sort);
            sort.setOnClickListener(v -> {
                newestFirst = !newestFirst;
                toast(getString(newestFirst ? R.string.album_sort_newest : R.string.album_sort_oldest));
                buildPhotoGrid(currentAlbum, showingAllPhotos);
            });

            TextView search = row.findViewById(R.id.action_center);
            search.setText(photoQuery.isEmpty() ? R.string.album_search : R.string.album_clear_search);
            search.setOnClickListener(v -> {
                if (photoQuery.isEmpty()) {
                    showPhotoSearch();
                } else {
                    photoQuery = "";
                    buildPhotoGrid(currentAlbum, showingAllPhotos);
                }
            });

            TextView upload = row.findViewById(R.id.action_right);
            upload.setText(showingAllPhotos ? R.string.album_create_new : R.string.album_upload);
            upload.setCompoundDrawablesWithIntrinsicBounds(
                showingAllPhotos ? R.drawable.ic_add : R.drawable.ic_upload, 0, 0, 0);
            upload.setCompoundDrawableTintList(ColorStateList.valueOf(color(R.color.surface_white)));
            upload.setOnClickListener(v -> {
                if (showingAllPhotos) {
                    askCreateAlbum();
                } else {
                    askUploadPhoto();
                }
            });
        }
    }

    private class PhotoHolder extends RecyclerView.ViewHolder {
        private final TextView title;
        private final TextView favorite;
        private final ImageView image;

        PhotoHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.photo_title);
            itemView.findViewById(R.id.photo_category).setVisibility(View.GONE);
            favorite = itemView.findViewById(R.id.photo_favorite);
            image = itemView.findViewById(R.id.photo_image);
        }

        void bind(AlbumPhoto photo) {
            title.setText(photo.title);
            favorite.setVisibility(photo.favorite ? View.VISIBLE : View.GONE);
            loadPhotoImage(image, photo);
            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), PhotoDetailActivity.class);
                intent.putExtra("photo_title", photo.title);
                intent.putExtra("photo_url", photo.url);
                intent.putExtra("photo_category", photo.category);
                intent.putExtra("photo_message", photo.familyMessage);
                intent.putExtra("photo_scene_tag", photo.sceneTag);
                intent.putExtra("photo_description", photo.description);
                intent.putExtra("photo_favorite", photo.favorite);
                intent.putExtra("photo_position", getBindingAdapterPosition());
                intent.putExtra("photo_total", albumPhotos.size());
                startActivity(intent);
            });
            itemView.setOnLongClickListener(v -> {
                familyAlbum().toggleFavorite(photo);
                buildPhotoGrid(currentAlbum, showingAllPhotos);
                return true;
            });
        }
    }

    private void toggleFirstFavorite() {
        if (albumPhotos.isEmpty()) {
            return;
        }
        familyAlbum().toggleFavorite(albumPhotos.get(0));
        toast(getString(albumPhotos.get(0).favorite
            ? R.string.album_memory_favorited : R.string.album_memory_unfavorited));
        buildPhotoGrid(currentAlbum, showingAllPhotos);
    }

    private TextView memoryAction(LinearLayout parent, String label, View.OnClickListener listener) {
        TextView action = (TextView) LayoutInflater.from(requireContext())
            .inflate(R.layout.view_album_memory_action, parent, false);
        action.setText(label);
        action.setOnClickListener(listener);
        return action;
    }

    private void loadAlbumCover(ImageView imageView, @Nullable Album album) {
        if (album != null && album.coverUrl != null && !album.coverUrl.isEmpty()) {
            try {
                Glide.with(imageView).load(Uri.parse(album.coverUrl)).centerCrop().into(imageView);
                return;
            } catch (Exception ignored) {
            }
            try {
                Glide.with(imageView).load(album.coverUrl).centerCrop().into(imageView);
                return;
            } catch (Exception ignored) {
            }
        }
        imageView.setImageResource(R.drawable.family_companion);
        imageView.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.primary_light));
    }

    private void loadPhotoImage(ImageView imageView, @Nullable AlbumPhoto photo) {
        if (photo != null && photo.url != null && !photo.url.isEmpty()) {
            try {
                Glide.with(imageView).load(Uri.parse(photo.url)).centerCrop().into(imageView);
                return;
            } catch (Exception ignored) {
            }
            try {
                Glide.with(imageView).load(photo.url).centerCrop().into(imageView);
                return;
            } catch (Exception ignored) {
            }
        }
        imageView.setImageResource(R.drawable.family_companion);
    }
}
