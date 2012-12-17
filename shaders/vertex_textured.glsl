#version 150 core

uniform mat4 projectionMatrix;
uniform mat4 modelviewMatrix;

in vec4 in_Position;
in vec2 in_Texture;

out vec2 pass_Texture;

void main(void) {
	gl_Position = projectionMatrix * modelviewMatrix * in_Position;	
	pass_Texture = in_Texture;
}
