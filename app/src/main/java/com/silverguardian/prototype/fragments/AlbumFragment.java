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
import androidx.recyclerview.widget.GridLayoutManager;
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
    private RecyclerView root;
    private static final int REQUEST_CAMERA = 702;
    private Uri pendingImageUri;
    private Uri cameraImageUri;
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
        root = (RecyclerView) inflater.inflate(R.layout.fragment_album, container, false);
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
        AlphaAnimation fade = new AlphaAnimation(0.35f, 1.0f);
        fade.setDuration(160);
        buildScreen.run();
        root.startAnimation(fade);
    }

    private void buildAlbumList() {
        currentAlbum = null;
        photoQuery = "";
        allPhotos.clear();
        allPhotos.addAll(familyAlbum().getPhotos());
        albumGroups.clear();
        Map<String, List<AlbumPhoto>> grouped = new LinkedHashMap<>();
        for (AlbumPhoto photo : allPhotos) {
            grouped.computeIfAbsent(photo.category != null ? photo.category : getString(R.string.album_other), key -> new ArrayList<>()).add(photo);
        }
        for (Map.Entry<String, List<AlbumPhoto>> entry : grouped.entrySet()) {
            if (!AlbumGroup.ALL_PHOTOS.equals(entry.getKey())) albumGroups.add(new AlbumGroup(entry.getKey(), entry.getValue()));
        }
        GridLayoutManager manager = new GridLayoutManager(requireContext(), 2);
        AlbumListAdapter pageAdapter = new AlbumListAdapter();
        manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override public int getSpanSize(int position) { return pageAdapter.getItemViewType(position) == AlbumListAdapter.TYPE_ALBUM ? 1 : 2; }
        });
        root.setLayoutManager(manager);
        root.setAdapter(pageAdapter);
    }

    private void buildPhotoGrid(String album) {
        currentAlbum = album;
        albumPhotos.clear();
        for (AlbumPhoto photo : allPhotos) {
            boolean inAlbum = AlbumGroup.ALL_PHOTOS.equals(album) || album.equals(photo.category);
            boolean matches = photoQuery.isEmpty() || (photo.title != null && photo.title.contains(photoQuery))
                || (photo.familyMessage != null && photo.familyMessage.contains(photoQuery));
            if (inAlbum && matches) albumPhotos.add(photo);
        }
        if (!newestFirst) Collections.reverse(albumPhotos);
        GridLayoutManager manager = new GridLayoutManager(requireContext(), 3);
        PhotoGridAdapter pageAdapter = new PhotoGridAdapter();
        manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override public int getSpanSize(int position) { return pageAdapter.getItemViewType(position) == PhotoGridAdapter.TYPE_PHOTO ? 1 : 3; }
        });
        root.setLayoutManager(manager);
        root.setAdapter(pageAdapter);
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
        pendingPickChip.setOnClickListener(v -> chooseImageSource());

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

    private void chooseImageSource() {
        new AlertDialog.Builder(requireContext())
            .setTitle("选择图片来源")
            .setItems(new String[]{"拍照", "从相册选择"}, (dialog, which) -> {
                if (which == 0) openCamera();
                else permHelper.requestPermissionThen(permHelper::openGallery);
            })
            .show();
    }

    private void openCamera() {
        try {
            java.io.File dir = new java.io.File(requireContext().getCacheDir(), "camera");
            if (!dir.exists()) dir.mkdirs();
            java.io.File file = java.io.File.createTempFile("album_", ".jpg", dir);
            cameraImageUri = androidx.core.content.FileProvider.getUriForFile(requireContext(), requireContext().getPackageName() + ".files", file);
            Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, cameraImageUri);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(intent, REQUEST_CAMERA);
        } catch (java.io.IOException e) {
            Toast.makeText(requireContext(), "无法打开相机", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri image = requestCode == REQUEST_CAMERA && resultCode == android.app.Activity.RESULT_OK ? cameraImageUri : GalleryPermissionHelper.handleActivityResult(requestCode, resultCode, data);
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

    private class AlbumListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        static final int TYPE_HEADER = 0, TYPE_ALBUM = 1, TYPE_FOOTER = 2;
        @Override public int getItemViewType(int position) { return position == 0 ? TYPE_HEADER : position == getItemCount() - 1 ? TYPE_FOOTER : TYPE_ALBUM; }
        @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
            if (type == TYPE_HEADER) return new AlbumListHeaderHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_album_list_header, parent, false));
            if (type == TYPE_FOOTER) return new AlbumCreateFooterHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_album_create_footer, parent, false));
            return new AlbumHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_album, parent, false));
        }
        @Override public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof AlbumListHeaderHolder) ((AlbumListHeaderHolder) holder).bind();
            else if (holder instanceof AlbumCreateFooterHolder) ((AlbumCreateFooterHolder) holder).bind();
            else ((AlbumHolder) holder).bind(albumGroups.get(position - 1));
        }
        @Override public int getItemCount() { return albumGroups.size() + 2; }
    }

    private class AlbumListHeaderHolder extends RecyclerView.ViewHolder {
        AlbumListHeaderHolder(View view) { super(view); }
        void bind() {
            View profile = itemView.findViewById(R.id.album_profile_header);
            ((TextView) profile.findViewById(R.id.header_title)).setText(R.string.album_title);
            ((TextView) profile.findViewById(R.id.header_subtitle)).setText(R.string.album_subtitle);
            ImageView avatar = profile.findViewById(R.id.header_avatar); avatar.setImageResource(R.drawable.elder_profile); avatar.setClipToOutline(true);
            View hero = itemView.findViewById(R.id.album_family_banner);
            ((ImageView) hero.findViewById(R.id.hero_image)).setImageResource(R.drawable.family_companion);
            ((TextView) hero.findViewById(R.id.hero_title)).setText(R.string.album_family_always);
            ((TextView) hero.findViewById(R.id.hero_subtitle)).setText(R.string.album_updated_today);
            View all = itemView.findViewById(R.id.album_all_photos_entry);
            ((TextView) all.findViewById(R.id.all_photos_title)).setText(R.string.album_all_photos);
            ((TextView) all.findViewById(R.id.all_photos_count)).setText(getString(R.string.album_photo_count_summary, allPhotos.size()));
            loadImg(all.findViewById(R.id.all_photos_cover), allPhotos.isEmpty() ? null : allPhotos.get(0));
            all.setOnClickListener(v -> transitionTo(() -> buildPhotoGrid(AlbumGroup.ALL_PHOTOS)));
            View section = itemView.findViewById(R.id.album_list_section_header);
            ((TextView) section.findViewById(R.id.album_section_title)).setText(R.string.album_my_albums);
            ((TextView) section.findViewById(R.id.album_section_count)).setText(getString(R.string.album_unit_count, albumGroups.size()));
        }
    }

    private class AlbumCreateFooterHolder extends RecyclerView.ViewHolder {
        AlbumCreateFooterHolder(View view) { super(view); }
        void bind() {
            TextView create = itemView.findViewById(R.id.album_create_footer_action);
            create.setText(R.string.album_create_new);
            create.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_add, 0, 0, 0);
            create.setOnClickListener(v -> askCreateAlbum());
        }
    }

    private class AlbumHolder extends RecyclerView.ViewHolder {
        private final TextView title, count; private final ImageView image;
        AlbumHolder(@NonNull View itemView) { super(itemView); title=itemView.findViewById(R.id.photo_title); count=itemView.findViewById(R.id.photo_category); itemView.findViewById(R.id.photo_message).setVisibility(View.GONE); itemView.findViewById(R.id.photo_favorite).setVisibility(View.GONE); image=itemView.findViewById(R.id.photo_image); }
        void bind(AlbumGroup group) {
            title.setText(group.name); count.setText(getString(R.string.album_photo_count_summary, group.count)); loadImg(image, group.cover);
            itemView.setOnClickListener(v -> transitionTo(() -> buildPhotoGrid(group.name)));
            itemView.setOnLongClickListener(v -> { new AlertDialog.Builder(requireContext()).setTitle(getString(R.string.album_delete_title, group.name)).setMessage(R.string.album_delete_confirm).setPositiveButton(R.string.common_delete,(d,w)->{for(AlbumPhoto p:new ArrayList<>(allPhotos))if(group.name.equals(p.category))familyAlbum().deletePhoto(p);buildAlbumList();}).setNegativeButton(R.string.common_cancel,null).show(); return true; });
        }
    }

    private class PhotoGridAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        static final int TYPE_HEADER=0, TYPE_PHOTO=1, TYPE_EMPTY=2, TYPE_FOOTER=3;
        @Override public int getItemViewType(int position) { if(position==0)return TYPE_HEADER; if(position==getItemCount()-1)return TYPE_FOOTER; return albumPhotos.isEmpty()?TYPE_EMPTY:TYPE_PHOTO; }
        @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,int type){
            LayoutInflater i=LayoutInflater.from(parent.getContext());
            if(type==TYPE_HEADER)return new PhotoHeaderHolder(i.inflate(R.layout.item_album_detail_header,parent,false));
            if(type==TYPE_FOOTER)return new PhotoFooterHolder(i.inflate(R.layout.item_album_photo_footer,parent,false));
            if(type==TYPE_EMPTY)return new EmptyAlbumHolder(i.inflate(R.layout.view_album_empty_state,parent,false));
            return new PhotoHolder(i.inflate(R.layout.item_photo_grid,parent,false));
        }
        @Override public void onBindViewHolder(@NonNull RecyclerView.ViewHolder h,int p){ if(h instanceof PhotoHeaderHolder)((PhotoHeaderHolder)h).bind(); else if(h instanceof PhotoFooterHolder)((PhotoFooterHolder)h).bind(); else if(h instanceof EmptyAlbumHolder)((EmptyAlbumHolder)h).bind(); else ((PhotoHolder)h).bind(albumPhotos.get(p-1)); }
        @Override public int getItemCount(){return 2+(albumPhotos.isEmpty()?1:albumPhotos.size());}
    }

    private class PhotoHeaderHolder extends RecyclerView.ViewHolder {
        PhotoHeaderHolder(View v){super(v);} void bind(){
            View header=itemView.findViewById(R.id.album_detail_page_header);TextView back=header.findViewById(R.id.header_back);back.setVisibility(View.VISIBLE);back.setText(R.string.common_back);back.setOnClickListener(v->transitionTo(AlbumFragment.this::buildAlbumList));((TextView)header.findViewById(R.id.header_title)).setText(currentAlbum);TextView more=header.findViewById(R.id.header_action);more.setVisibility(View.VISIBLE);more.setText(R.string.common_more);more.setOnClickListener(v->toast(getString(R.string.album_manage_hint)));
            View hero=itemView.findViewById(R.id.album_detail_hero);loadImg(hero.findViewById(R.id.hero_image),albumPhotos.isEmpty()?null:albumPhotos.get(0));((TextView)hero.findViewById(R.id.hero_title)).setText(currentAlbum);((TextView)hero.findViewById(R.id.hero_subtitle)).setText(getString(R.string.album_hero_subtitle,albumPhotos.size()));
            View memory=itemView.findViewById(R.id.album_detail_memory);((TextView)memory.findViewById(R.id.card_title)).setText(R.string.album_memory_note);((TextView)memory.findViewById(R.id.card_body)).setText(R.string.album_memory_body);LinearLayout actions=memory.findViewById(R.id.card_actions);actions.removeAllViews();actions.addView(memoryAction(actions,getString(R.string.album_favorite),v->toggleFirstFavorite()));actions.addView(memoryAction(actions,getString(R.string.album_share),v->toast(getString(R.string.album_share_opened))));actions.addView(memoryAction(actions,getString(R.string.album_voice_memory),v->toast(getString(R.string.album_voice_hint))));
            View section=itemView.findViewById(R.id.album_detail_section_header);((TextView)section.findViewById(R.id.album_section_title)).setText(R.string.album_all_photos);((TextView)section.findViewById(R.id.album_section_count)).setText(getString(R.string.album_photo_count_summary,albumPhotos.size()));
        }
    }

    private class EmptyAlbumHolder extends RecyclerView.ViewHolder { EmptyAlbumHolder(View v){super(v);} void bind(){((TextView)itemView.findViewById(R.id.album_empty_title)).setText(R.string.album_empty_title);((TextView)itemView.findViewById(R.id.album_empty_subtitle)).setText(R.string.album_empty_subtitle);TextView a=itemView.findViewById(R.id.album_empty_action);a.setText(R.string.album_upload_first);a.setOnClickListener(v->askUploadPhoto());} }

    private class PhotoFooterHolder extends RecyclerView.ViewHolder { PhotoFooterHolder(View v){super(v);} void bind(){View row=itemView.findViewById(R.id.album_footer_action_row);TextView sort=row.findViewById(R.id.action_left);sort.setText(R.string.album_sort);sort.setOnClickListener(v->{newestFirst=!newestFirst;buildPhotoGrid(currentAlbum);});TextView search=row.findViewById(R.id.action_center);search.setText(photoQuery.isEmpty()?R.string.album_search:R.string.album_clear_search);search.setOnClickListener(v->{if(photoQuery.isEmpty())showPhotoSearch();else{photoQuery="";buildPhotoGrid(currentAlbum);}});TextView upload=row.findViewById(R.id.action_right);upload.setText(R.string.album_upload);upload.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_upload,0,0,0);upload.setOnClickListener(v->askUploadPhoto());} }

    private class PhotoHolder extends RecyclerView.ViewHolder {
        private final TextView title,favorite;private final ImageView image;
        PhotoHolder(@NonNull View itemView){super(itemView);title=itemView.findViewById(R.id.photo_title);itemView.findViewById(R.id.photo_category).setVisibility(View.GONE);favorite=itemView.findViewById(R.id.photo_favorite);image=itemView.findViewById(R.id.photo_image);}
        void bind(AlbumPhoto photo){title.setText(photo.title);favorite.setVisibility(photo.favorite?View.VISIBLE:View.GONE);loadImg(image,photo);itemView.setOnClickListener(v->{Intent i=new Intent(requireContext(),PhotoDetailActivity.class);i.putExtra("photo_title",photo.title);i.putExtra("photo_url",photo.url);i.putExtra("photo_category",photo.category);i.putExtra("photo_message",photo.familyMessage);i.putExtra("photo_scene_tag",photo.sceneTag);i.putExtra("photo_description",photo.description);i.putExtra("photo_favorite",photo.favorite);i.putExtra("photo_position",getBindingAdapterPosition());i.putExtra("photo_total",albumPhotos.size());startActivity(i);});itemView.setOnLongClickListener(v->{familyAlbum().toggleFavorite(photo);buildPhotoGrid(currentAlbum);return true;});}
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