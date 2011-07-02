uniform sampler2D m_Texture1;
uniform sampler2D m_Texture2;

varying vec2 texCoord;

uniform float m_Value;

void main() {
       vec4 texVal1 = texture2D(m_Texture1, texCoord);
       vec4 texVal2 = texture2D(m_Texture2, texCoord);


       gl_FragColor = (texVal1 * m_Value) + (texVal2 * (1.0 - mValue));

}
