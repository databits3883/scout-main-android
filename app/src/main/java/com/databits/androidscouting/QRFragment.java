package com.databits.androidscouting;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.databits.androidscouting.databinding.FragmentQRBinding;
import com.github.sumimakito.awesomeqr.AwesomeQrRenderer;
import com.github.sumimakito.awesomeqr.RenderResult;
import com.github.sumimakito.awesomeqr.option.RenderOption;
import com.github.sumimakito.awesomeqr.option.color.ColorQR;
import com.github.sumimakito.awesomeqr.option.logo.Logo;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

public class QRFragment extends Fragment {

    private FragmentQRBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentQRBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        NavController controller = NavHostFragment.findNavController(QRFragment.this);

        binding.buttonBack.setOnClickListener(view1 -> controller
                .navigate(R.id.action_QRFragment_to_crowdScoutFragment));

        generateQrCode();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void generateQrCode() {
        Bitmap logobit = BitmapFactory
                .decodeResource(requireContext().getResources(), R.drawable.logo);

        Logo logo = new Logo();

        logo.setBitmap(logobit);
//    logo.setBorderRadius(10); // radius for logo's corners
//    logo.setBorderWidth(10); // width of the border to be added around the logo
        logo.setScale(0.4f); // scale for the logo in the QR code
        logo.setClippingRect(new RectF(0, 0, 200, 200)); // crop the logo image before applying it to the QR code

        ColorQR color = new ColorQR();
        color.setLight(getResources().getColor(R.color.white, null)); // for blank spaces
        color.setDark(getResources().getColor(R.color.green_900, null)); // for non-blank spaces
        color.setBackground(getResources().getColor(R.color.white, null)); // for the background (will be overridden by background images, if set)
        color.setAuto(false); // set to true to automatically pick out colors from the background image (will only work if background image is present)

        RenderOption renderOption = new RenderOption();
        renderOption.setContent("7028,1,1,0,8,0,0,9,2,2,1,1,0,0,Sean,3 high auto missed first 2"); // content to encode
        renderOption.setSize(1500); // size of the final QR code image
        renderOption.setBorderWidth(5); // width of the empty space around the QR code
        renderOption.setEcl(ErrorCorrectionLevel.H); // (optional) specify an error correction level
        renderOption.setPatternScale(0.5f); // (optional) specify a scale for patterns
        renderOption.setRoundedPatterns(false); // (optional) if true, blocks will be drawn as dots instead
        renderOption.setClearBorder(true); // if set to true, the background will NOT be drawn on the border area
        renderOption.setColorQR(color); // set a colorQR palette for the QR code
        renderOption.setLogo(logo); // set a logo, keep reading to find more about it
        try {
            RenderResult render = AwesomeQrRenderer.render(renderOption);
            ImageView qr_img = requireView().findViewById(R.id.qr_img);
            qr_img.setImageBitmap(render.getBitmap());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}