package com.silverguardian.prototype.fragments;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.bumptech.glide.Glide;
import com.silverguardian.prototype.PhotoDetailActivity;
import com.silverguardian.prototype.R;
import com.silverguardian.prototype.models.AlbumGroup;
import com.silverguardian.prototype.models.AlbumPhoto;
import com.silverguardian.prototype.utils.FormFieldFactory;
import com.silverguardian.prototype.utils.GalleryPermissionHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AlbumFragment extends BaseFragment {

    private GalleryPermissionHelper permHelper;
    private LinearLayout root;
    private Uri pendingImageUri;
    private ImageView pendingPreview;
    private TextView pendingPickChip;
    private String currentAlbum;
    private String photoQuery = "";
    private boolean newestFirst = true;

    private final List<AlbumPhoto> allPhotos = new ArrayList<>();
    private final List<AlbumGroup> albumGroups = new ArrayList<>();
    private final List<AlbumPhoto> albumPhotos = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root = (LinearLayout) inflater.inflate(R.layout.fragment_album, container, false);
        permHelper = new GalleryPermissionHelper(this);

        String initialAlbum = getArguments() == null ? null : getArguments().getString("album_name");
        if (initialAlbum == null || initialAlbum.trim().isEmpty()) {
            buildAlbumList();
        } else {
            allPhotos.clear();
            allPhotos.addAll(familyAlbum().getPhotos());
            buildPhotoGrid(initialAlbum);
        }
        return root;
    }

    private void transitionTo(Runnable buildScreen) {
        AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.3f);
        fadeOut.setDuration(120);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override public void onAnimationStart(Animation animation) { }
            @Override public void onAnimationRepeat(Animation animation) { }
            @Override public void onAnimationEnd(Animation animation) {
                root.removeAllViews();
                buildScreen.run();
                AlphaAnimation fadeIn = new AlphaAnimation(0.3f, 1.0f);
                fadeIn.setDuration(150);
                root.startAnimation(fadeIn);
            }
        });
        root.startAnimation(fadeOut);
    }

    private void buildAlbumList() {
        root.removeAllViews();
        currentAlbum = null;
        photoQuery = "";

        root.addView(createProfileHeader(
            getString(R.string.album_title),
            getString(R.string.album_subtitle),
            R.drawable.elder_profile,
            getString(R.string.album_profile_desc)
        ));
        root.addView(familyBanner());

        allPhotos.clear();
        allPhotos.addAll(familyAlbum().getPhotos());
        albumGroups.clear();
        Map<String, List<AlbumPhoto>> grouped = new LinkedHashMap<>();
        for (AlbumPhoto photo : allPhotos) {
            grouped.computeIfAbsent(photo.category != null ? photo.category : getString(R.string.album_other), key -> new ArrayList<>()).add(photo);
        }
        for (Map.Entry<String, List<AlbumPhoto>> entry : grouped.entrySet()) {
            if (!AlbumGroup.ALL_PHOTOS.equals(entry.getKey())) {
                albumGroups.add(new AlbumGroup(entry.getKey(), entry.getValue()));
            }
        }

        root.addView(allPhotosEntry());

        View sectionHeader = inflateShared(R.layout.view_album_section_header);
        ((TextView) sectionHeader.findViewById(R.id.album_section_title)).setText(R.string.album_my_albums);
        ((TextView) sectionHeader.findViewById(R.id.album_section_count)).setText(getString(R.string.album_unit_count, albumGroups.size()));
        root.addView(sectionHeader);

        RecyclerView list = new RecyclerView(requireContext());
        list.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        list.setAdapter(new AlbumListAdapter());
        root.addView(list, lp(-1, 0, 1));

        TextView create = inflatePrimaryActionButton(getString(R.string.album_create_new), R.drawable.ic_add);
        create.setOnClickListener(v -> askCreateAlbum());
        LinearLayout.LayoutParams createParams = new LinearLayout.LayoutParams(-1, getResources().getDimensionPixelSize(R.dimen.button_height));
        createParams.topMargin = getResources().getDimensionPixelSize(R.dimen.section_gap);
        root.addView(create, createParams);
    }

    private void buildPhotoGrid(String album) {
        root.removeAllViews();
        currentAlbum = album;

        root.addView(createPageHeader(
            getString(R.string.common_back),
            album,
            getString(R.string.common_more),
            v -> transitionTo(this::buildAlbumList),
            v -> toast(getString(R.string.album_manage_hint))
        ));

        albumPhotos.clear();
        for (AlbumPhoto photo : allPhotos) {
            boolean inAlbum = AlbumGroup.ALL_PHOTOS.equals(album) || album.equals(photo.category);
            boolean matches = photoQuery.isEmpty()
                || (photo.title != null && photo.title.contains(photoQuery))
                || (photo.familyMessage != null && photo.familyMessage.contains(photoQuery));
            if (inAlbum && matches) {
                albumPhotos.add(photo);
            }
        }
        if (!newestFirst) {
            Collections.reverse(albumPhotos);
        }

        if (albumPhotos.isEmpty()) {
            root.addView(emptyAlbumState(), lp(-1, 0, 1));
            return;
        }

        root.addView(albumHero());
        root.addView(memoryNote());

        View photoHeader = inflateShared(R.layout.view_album_section_header);
        ((TextView) photoHeader.findViewById(R.id.album_section_title)).setText(R.string.album_all_photos);
        ((TextView) photoHeader.findViewById(R.id.album_section_count)).setText(getString(R.string.album_photo_count_summary, albumPhotos.size()));
        root.addView(photoHeader);

        RecyclerView grid = new RecyclerView(requireContext());
        grid.setLayoutManager(new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL));
        grid.setAdapter(new PhotoGridAdapter());
        root.addView(grid, lp(-1, 0, 1));

        root.addView(photoToolbar());
    }

    private View emptyAlbumState() {
        View empty = inflateShared(R.layout.view_album_empty_state);
        ((TextView) empty.findViewById(R.id.album_empty_title)).setText(R.string.album_empty_title);
        ((TextView) empty.findViewById(R.id.album_empty_subtitle)).setText(R.string.album_empty_subtitle);
        TextView upload = empty.findViewById(R.id.album_empty_action);
        upload.setText(R.string.album_upload_first);
        upload.setOnClickListener(v -> askUploadPhoto());
        return empty;
    }

    private View familyBanner() {
        View hero = createHeroBanner(
            getString(R.string.album_family_always),
            getString(R.string.album_updated_today),
            getString(R.string.album_family_banner_desc),
            null,
            R.drawable.family_companion,
            R.dimen.hero_banner_height_small
        );
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, getResources().getDimensionPixelSize(R.dimen.hero_banner_height_small));
        params.bottomMargin = getResources().getDimensionPixelSize(R.dimen.album_banner_spacing_bottom);
        hero.setLayoutParams(params);
        return hero;
    }

    private View allPhotosEntry() {
        View card = inflateShared(R.layout.view_album_all_photos_entry);
        card.setContentDescription(getString(R.string.album_all_photos_content_desc, allPhotos.size()));
        card.setOnClickListener(v -> transitionTo(() -> buildPhotoGrid(AlbumGroup.ALL_PHOTOS)));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, getResources().getDimensionPixelSize(R.dimen.album_all_photos_entry_height));
        params.bottomMargin = getResources().getDimensionPixelSize(R.dimen.section_gap);
        card.setLayoutParams(params);

        ((TextView) card.findViewById(R.id.all_photos_title)).setText(R.string.album_all_photos);
        ((TextView) card.findViewById(R.id.all_photos_count)).setText(getString(R.string.album_photo_count_summary, allPhotos.size()));
        ImageView cover = card.findViewById(R.id.all_photos_cover);
        AlbumPhoto first = allPhotos.isEmpty() ? null : allPhotos.get(0);
        loadImg(cover, first);
        return card;
    }

    private View albumHero() {
        View hero = createHeroBanner(
            currentAlbum,
            getString(R.string.album_hero_subtitle, albumPhotos.size()),
            getString(R.string.album_hero_desc, currentAlbum),
            albumPhotos.isEmpty() ? null : albumPhotos.get(0),
            R.drawable.family_companion,
            R.dimen.hero_banner_height_large
        );
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, getResources().getDimensionPixelSize(R.dimen.hero_banner_height_large));
        params.bottomMargin = getResources().getDimensionPixelSize(R.dimen.hero_banner_spacing_bottom);
        hero.setLayoutParams(params);
        return hero;
    }

    private View memoryNote() {
        LinearLayout card = (LinearLayout) inflateShared(R.layout.view_section_card);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.bottomMargin = getResources().getDimensionPixelSize(R.dimen.album_memory_card_spacing_bottom);
        card.setLayoutParams(params);

        ((TextView) card.findViewById(R.id.card_title)).setText(R.string.album_memory_note);
        ((TextView) card.findViewById(R.id.card_body)).setText(R.string.album_memory_body);

        LinearLayout actions = card.findViewById(R.id.card_actions);
        actions.addView(memoryAction(actions, getString(R.string.album_favorite), v -> toggleFirstFavorite()));
        actions.addView(memoryAction(actions, getString(R.string.album_share), v -> toast(getString(R.string.album_share_opened))));
        actions.addView(memoryAction(actions, getString(R.string.album_voice_memory), v -> toast(getString(R.string.album_voice_hint))));
        return card;
    }

    private TextView memoryAction(LinearLayout parent, String label, View.OnClickListener listener) {
        TextView action = (TextView) LayoutInflater.from(requireContext())
            .inflate(R.layout.view_album_memory_action, parent, false);
        action.setText(label);
        action.setContentDescription(label);
        action.setOnClickListener(listener);
        return action;
    }

    private void toggleFirstFavorite() {
        if (albumPhotos.isEmpty()) {
            return;
        }
        familyAlbum().toggleFavorite(albumPhotos.get(0));
        toast(getString(albumPhotos.get(0).favorite ? R.string.album_memory_favorited : R.string.album_memory_unfavorited));
        buildPhotoGrid(currentAlbum);
    }

    private View photoToolbar() {
        LinearLayout toolbar = (LinearLayout) inflateShared(R.layout.view_action_row);

        TextView sort = toolbar.findViewById(R.id.action_left);
        sort.setText(R.string.album_sort);
        sort.setOnClickListener(v -> {
            newestFirst = !newestFirst;
            toast(getString(newestFirst ? R.string.album_sort_newest : R.string.album_sort_oldest));
            buildPhotoGrid(currentAlbum);
        });

        TextView search = toolbar.findViewById(R.id.action_center);
        search.setText(photoQuery.isEmpty() ? R.string.album_search : R.string.album_clear_search);
        search.setOnClickListener(v -> {
            if (photoQuery.isEmpty()) {
                showPhotoSearch();
            } else {
                photoQuery = "";
                buildPhotoGrid(currentAlbum);
            }
        });

        TextView upload = toolbar.findViewById(R.id.action_right);
        upload.setText(R.string.album_upload);
        upload.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_upload, 0, 0, 0);
        upload.setCompoundDrawableTintList(ColorStateList.valueOf(color(R.color.surface_white)));
        upload.setCompoundDrawablePadding(getResources().getDimensionPixelSize(R.dimen.action_row_gap));
        upload.setOnClickListener(v -> askUploadPhoto());

        return toolbar;
    }

    private void showPhotoSearch() {
        LinearLayout form = (LinearLayout) LayoutInflater.from(requireContext())
            .inflate(R.layout.view_dialog_form_container, null, false);
        EditText input = FormFieldFactory.addTextField(requireContext(), form, getString(R.string.album_search_title), getString(R.string.album_search_hint), false);
        new AlertDialog.Builder(requireContext())
            .setTitle(R.string.album_search_title)
            .setView(form)
            .setPositiveButton(R.string.album_search, (dialog, which) -> {
                photoQuery = input.getText().toString().trim();
                buildPhotoGrid(currentAlbum);
            })
            .setNegativeButton(R.string.common_cancel, null)
            .show();
    }

    private void askCreateAlbum() {
        LinearLayout form = (LinearLayout) LayoutInflater.from(requireContext())
            .inflate(R.layout.view_dialog_form_container, null, false);
        EditText input = FormFieldFactory.addTextField(requireContext(), form, getString(R.string.album_name), getString(R.string.album_name_hint), false);
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
            buildPhotoGrid(name);
            dialog.dismiss();
        }));
        dialog.show();
    }

    private void askUploadPhoto() {
        pendingImageUri = null;

        View form = LayoutInflater.from(requireContext()).inflate(R.layout.view_album_upload_form, null, false);
        pendingPreview = form.findViewById(R.id.album_upload_preview);
        pendingPickChip = form.findViewById(R.id.album_upload_pick_chip);
        LinearLayout fieldsContainer = form.findViewById(R.id.album_upload_fields_container);

        pendingPreview.setClipToOutline(true);
        pendingPreview.setBackgroundTintList(ColorStateList.valueOf(color(R.color.surface_mint)));
        pendingPreview.setImageResource(R.drawable.ic_album);

        EditText titleInput = FormFieldFactory.addTextField(requireContext(), fieldsContainer, getString(R.string.album_photo_title), getString(R.string.album_name_hint), false);
        EditText messageInput = FormFieldFactory.addTextField(requireContext(), fieldsContainer, getString(R.string.album_message_label), getString(R.string.album_message_hint), true);
        pendingPickChip.setOnClickListener(v -> permHelper.requestPermissionThen(permHelper::openGallery));

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.album_upload_to, currentAlbum))
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
            familyAlbum().addPhoto(title, currentAlbum, messageInput.getText().toString().trim());
            AlbumPhoto added = familyAlbum().getPhotos().isEmpty() ? null : familyAlbum().getPhotos().get(0);
            if (added != null) {
                added.url = pendingImageUri.toString();
            }
            buildPhotoGrid(currentAlbum);
            Toast.makeText(requireContext(), R.string.album_upload_success, Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        }));
        dialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri image = GalleryPermissionHelper.handleActivityResult(requestCode, resultCode, data);
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
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (!permHelper.onRequestPermissionsResult(requestCode, grantResults)) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private class AlbumListAdapter extends RecyclerView.Adapter<AlbumHolder> {
        @NonNull
        @Override
        public AlbumHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new AlbumHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_album, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull AlbumHolder holder, int position) {
            holder.bind(albumGroups.get(position));
        }

        @Override
        public int getItemCount() {
            return albumGroups.size();
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

        void bind(AlbumGroup group) {
            boolean isAllPhotos = AlbumGroup.ALL_PHOTOS.equals(group.name);
            title.setText(isAllPhotos ? getString(R.string.album_all_photos) : group.name);
            count.setText(isAllPhotos ? getString(R.string.album_all_photo_count_summary, group.count) : getString(R.string.album_photo_count_summary, group.count));
            title.setTypeface(null, isAllPhotos ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
            if (isAllPhotos) {
                title.setTextColor(color(R.color.primary_dark));
                count.setTextColor(color(R.color.primary));
            } else {
                title.setTextColor(color(R.color.text_primary));
                count.setTextColor(color(R.color.text_secondary));
            }
            loadImg(image, group.cover);
            itemView.setContentDescription(getString(R.string.album_group_content_desc, group.name, group.count));
            itemView.setOnClickListener(v -> transitionTo(() -> buildPhotoGrid(group.name)));
            if (!AlbumGroup.ALL_PHOTOS.equals(group.name)) {
                itemView.setOnLongClickListener(v -> {
                    new AlertDialog.Builder(requireContext())
                        .setTitle(getString(R.string.album_delete_title, group.name))
                        .setMessage(R.string.album_delete_confirm)
                        .setPositiveButton(R.string.common_delete, (dialog, which) -> {
                            for (AlbumPhoto photo : new ArrayList<>(allPhotos)) {
                                if (group.name.equals(photo.category)) {
                                    familyAlbum().deletePhoto(photo);
                                }
                            }
                            buildAlbumList();
                        })
                        .setNegativeButton(R.string.common_cancel, null)
                        .show();
                    return true;
                });
            }
        }
    }

    private class PhotoGridAdapter extends RecyclerView.Adapter<PhotoHolder> {
        @NonNull
        @Override
        public PhotoHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new PhotoHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_photo_grid, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull PhotoHolder holder, int position) {
            holder.bind(albumPhotos.get(position));
        }

        @Override
        public int getItemCount() {
            return albumPhotos.size();
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
            loadImg(image, photo);
            itemView.setContentDescription(photo.title + (photo.favorite ? getString(R.string.album_favorited_suffix) : ""));
            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), PhotoDetailActivity.class);
                intent.putExtra("photo_title", photo.title);
                intent.putExtra("photo_url", photo.url);
                intent.putExtra("photo_category", photo.category);
                intent.putExtra("photo_message", photo.familyMessage);
                intent.putExtra("photo_scene_tag", photo.sceneTag);
                intent.putExtra("photo_description", photo.description);
                intent.putExtra("photo_favorite", photo.favorite);
                intent.putExtra("photo_position", getAdapterPosition());
                intent.putExtra("photo_total", albumPhotos.size());
                startActivity(intent);
            });
            itemView.setOnLongClickListener(v -> {
                familyAlbum().toggleFavorite(photo);
                buildPhotoGrid(currentAlbum);
                return true;
            });
        }
    }

    private void loadImg(ImageView imageView, @Nullable AlbumPhoto photo) {
        if (photo != null && photo.url != null && !photo.url.isEmpty()) {
            try {
                Glide.with(imageView).load(Uri.parse(photo.url)).centerCrop().into(imageView);
                return;
            } catch (Exception ignored) {
                // Fall through to the local placeholder below.
            }
        }
        imageView.setImageResource(R.drawable.family_companion);
    }

    private TextView inflatePrimaryActionButton(String label, int iconRes) {
        TextView button = (TextView) LayoutInflater.from(requireContext())
            .inflate(R.layout.view_primary_action_button, root, false);
        button.setText(label);
        button.setContentDescription(label);
        button.setCompoundDrawablesWithIntrinsicBounds(iconRes, 0, 0, 0);
        button.setCompoundDrawableTintList(ColorStateList.valueOf(color(R.color.surface_white)));
        return button;
    }

    private int dimen(int resId) {
        return getResources().getDimensionPixelSize(resId);
    }

    private LinearLayout.LayoutParams lp(int width, int height, float weight) {
        return new LinearLayout.LayoutParams(width, height, weight);
    }

    private View inflateShared(int layoutRes) {
        return LayoutInflater.from(requireContext()).inflate(layoutRes, root, false);
    }

    private View createPageHeader(String backLabel, String title, String actionLabel,
                                  View.OnClickListener backClick, View.OnClickListener actionClick) {
        View header = inflateShared(R.layout.view_page_header);
        TextView back = header.findViewById(R.id.header_back);
        TextView pageTitle = header.findViewById(R.id.header_title);
        TextView action = header.findViewById(R.id.header_action);

        back.setVisibility(View.VISIBLE);
        back.setText(backLabel);
        back.setOnClickListener(backClick);

        pageTitle.setText(title);

        action.setVisibility(View.VISIBLE);
        action.setText(actionLabel);
        action.setOnClickListener(actionClick);
        return header;
    }

    private View createProfileHeader(String title, String subtitle, int avatarRes, String avatarDescription) {
        View header = inflateShared(R.layout.view_profile_header);
        ((TextView) header.findViewById(R.id.header_title)).setText(title);
        ((TextView) header.findViewById(R.id.header_subtitle)).setText(subtitle);

        ImageView avatar = header.findViewById(R.id.header_avatar);
        avatar.setImageResource(avatarRes);
        avatar.setClipToOutline(true);
        avatar.setContentDescription(avatarDescription);
        return header;
    }

    private View createHeroBanner(String title, String subtitle, String description,
                                  @Nullable AlbumPhoto photo, int fallbackRes, int heightRes) {
        FrameLayout hero = (FrameLayout) inflateShared(R.layout.view_hero_banner);
        hero.setLayoutParams(new LinearLayout.LayoutParams(-1, getResources().getDimensionPixelSize(heightRes)));

        ImageView image = hero.findViewById(R.id.hero_image);
        if (photo != null) {
            loadImg(image, photo);
        } else {
            image.setImageResource(fallbackRes);
        }
        image.setContentDescription(description);

        ((TextView) hero.findViewById(R.id.hero_title)).setText(title);
        ((TextView) hero.findViewById(R.id.hero_subtitle)).setText(subtitle);
        return hero;
    }
}