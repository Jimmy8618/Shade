package amirz.shade.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.android.launcher3.R;
import com.android.launcher3.ResourceUtils;
import com.android.launcher3.icons.LauncherIcons;
import com.android.launcher3.util.Themes;
import com.android.searchlauncher.SmartspaceHostView;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class ThemedSmartspaceHostView extends SmartspaceHostView {
    private DoubleShadowTextView mDstv;

    public ThemedSmartspaceHostView(Context context) {
        super(context);
    }

    public void setSampleDoubleShadowTextView(DoubleShadowTextView dstv) {
        mDstv = dstv;
        overrideView();
    }

    @Override
    public void updateAppWidget(RemoteViews remoteViews) {
        super.updateAppWidget(remoteViews);
        if (mDstv != null) {
            overrideView();
        }
    }

    private void overrideView() {
        Context context = getContext();

        int textColor = Themes.getAttrColor(context, R.attr.workspaceTextColor);
        int dividerSize = context.getResources().getDimensionPixelSize(R.dimen.smartspaceDivider) + 1;

        if (getChildCount() == 1) {
            View topLevel = getChildAt(0);
            if (topLevel instanceof RelativeLayout) {
                RelativeLayout rl = (RelativeLayout) topLevel;
                if (rl.getChildCount() == 1) {
                    View internal = rl.getChildAt(0);
                    if (internal instanceof LinearLayout) {
                        LinearLayout internall = (LinearLayout) internal;
                        if (internall.getChildCount() == 2
                                && internall.getOrientation() == LinearLayout.VERTICAL) {
                            overrideLayout(internall);
                        }
                    }
                }
            }
        }

        overrideView(this, textColor, dividerSize);
    }

    private void overrideLayout(LinearLayout l) {
        ViewGroup.LayoutParams llp = l.getLayoutParams();
        llp.height = MATCH_PARENT;
        l.setLayoutParams(llp);
        l.setClipChildren(false);

        View topView = l.getChildAt(0);
        View bottomView = l.getChildAt(1);
        if (topView instanceof LinearLayout && bottomView instanceof LinearLayout) {
            LinearLayout topl = (LinearLayout) topView;
            LinearLayout bottoml = (LinearLayout) bottomView;

            LinearLayout.LayoutParams lp =
                    new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
            lp.gravity = Gravity.BOTTOM;
            for (int i = 0; i < topl.getChildCount(); i++) {
                topl.getChildAt(i).setLayoutParams(lp);
            }

            Context context = getContext();
            if (Themes.getAttrBoolean(context, R.attr.isWorkspaceDarkText)
                    && bottoml.getChildCount() > 0) {
                View v = bottoml.getChildAt(0);
                if (v instanceof ImageView) {
                    int textColor = Themes.getAttrColor(context, R.attr.workspaceTextColor);
                    ImageView iv = (ImageView) v;
                    iv.setColorFilter(textColor);
                }
            }

            LinearLayout.LayoutParams toplp
                    = (LinearLayout.LayoutParams) topl.getLayoutParams();
            toplp.weight = 1f;
            toplp.setMargins(0, 0, 0, 0);
            topl.setLayoutParams(toplp);

            LinearLayout.LayoutParams bottomlp
                    = (LinearLayout.LayoutParams) bottoml.getLayoutParams();
            bottomlp.weight = 1f;
            bottomlp.setMargins(0, 0, 0, 0);
            bottoml.setLayoutParams(bottomlp);
        }
    }

    private TextView replaceTextView(TextView tv) {
        return mDstv == null
                ? tv
                : mDstv.cloneTextView(tv);
    }

    private void overrideView(View v, int textColor, int maxDividerSize) {
        if (!(v instanceof ViewGroup)) {
            return;
        }

        ViewGroup vg = (ViewGroup) v;
        vg.setClipChildren(false);
        for (int i = 0; i < vg.getChildCount(); i++) {
            View vc = vg.getChildAt(i);
            if (vc instanceof TextView) {
                ViewGroup.LayoutParams lp = vc.getLayoutParams();
                vg.removeViewAt(i);
                vg.addView(replaceTextView((TextView) vc), i, lp);
            } else if (vc instanceof ImageView) {
                ImageView iv = (ImageView) vc;
                ViewGroup.LayoutParams lp = iv.getLayoutParams();
                if (lp.height <= maxDividerSize || lp.width <= maxDividerSize) {
                    iv.setBackgroundColor(textColor);
                } else {
                    Drawable d = iv.getDrawable();
                    if (d instanceof BitmapDrawable) {
                        BitmapDrawable bd = (BitmapDrawable) d;
                        Bitmap bm = bd.getBitmap();

                        Context context = getContext();
                        int shadowSize = ResourceUtils.pxFromDp(2f,
                                context.getResources().getDisplayMetrics());

                        Bitmap result = Bitmap.createBitmap(bm.getWidth() + 2 * shadowSize,
                                bm.getHeight() + 2 * shadowSize, Bitmap.Config.ARGB_8888);

                        Canvas canvas = new Canvas();
                        canvas.setBitmap(result);
                        canvas.translate(shadowSize, shadowSize);

                        boolean shadowActive = !Themes.getAttrBoolean(
                                context, R.attr.isWorkspaceDarkText);
                        LauncherIcons li = LauncherIcons.obtain(context);
                        li.getShadowGenerator().recreateIcon(
                                bm,
                                new BlurMaskFilter(shadowSize, BlurMaskFilter.Blur.NORMAL),
                                shadowActive ? 0x64 : 0x10,
                                shadowActive ? 0x7C : 0,
                                canvas);
                        li.recycle();

                        iv.setImageBitmap(result);
                        iv.getLayoutParams().height = (int) ((float) iv.getLayoutParams().height
                                * result.getHeight() / bm.getHeight());
                        iv.getLayoutParams().width = (int) ((float) iv.getLayoutParams().width
                                * result.getWidth() / bm.getWidth());
                    }
                }
            } else {
                overrideView(vc, textColor, maxDividerSize);
            }
        }
    }
}
