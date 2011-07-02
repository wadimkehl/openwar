#import "Common/ShaderLib/MultiSample.glsllib"

uniform COLORTEXTURE m_Texture1, m_Texture2;
uniform float m_Value;

in vec2 texCoord;

void main() {
    vec4 texVal1 = getColor(m_Texture1, texCoord);
    vec4 texVal2 = getColor(m_Texture2, texCoord);
    
    gl_FragColor = (texVal1 * m_Value) + (texVal2 * (1.0 - mValue));
}