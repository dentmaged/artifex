package org.anchor.client.engine.renderer.ssao;

import org.anchor.client.engine.renderer.blur.BlurShader;

public class SSAOBlurShader extends BlurShader {

    private static SSAOBlurShader instance = new SSAOBlurShader();

    public SSAOBlurShader() {
        super("ssaoBlur");
    }

    public static SSAOBlurShader getInstance() {
        return instance;
    }

}
