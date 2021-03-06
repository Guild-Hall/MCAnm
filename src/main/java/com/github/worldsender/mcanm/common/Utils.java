package com.github.worldsender.mcanm.common;

import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL20.GL_ATTACHED_SHADERS;
import static org.lwjgl.opengl.GL20.GL_COMPILE_STATUS;
import static org.lwjgl.opengl.GL20.GL_INFO_LOG_LENGTH;
import static org.lwjgl.opengl.GL20.GL_LINK_STATUS;
import static org.lwjgl.opengl.GL20.GL_SHADER_TYPE;
import static org.lwjgl.opengl.GL20.glCompileShader;
import static org.lwjgl.opengl.GL20.glCreateProgram;
import static org.lwjgl.opengl.GL20.glCreateShader;
import static org.lwjgl.opengl.GL20.glDeleteShader;
import static org.lwjgl.opengl.GL20.glGetAttachedShaders;
import static org.lwjgl.opengl.GL20.glGetProgramInfoLog;
import static org.lwjgl.opengl.GL20.glGetProgrami;
import static org.lwjgl.opengl.GL20.glGetShaderInfoLog;
import static org.lwjgl.opengl.GL20.glGetShaderi;
import static org.lwjgl.opengl.GL20.glLinkProgram;
import static org.lwjgl.opengl.GL20.glShaderSource;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnmappableCharacterException;
import java.util.Arrays;

import org.lwjgl.opengl.GL20;

import com.github.worldsender.mcanm.common.util.math.Matrix4f;
import com.github.worldsender.mcanm.common.util.math.Quat4f;
import com.github.worldsender.mcanm.common.util.math.Vector2f;
import com.github.worldsender.mcanm.common.util.math.Vector3f;
import com.google.common.primitives.Longs;

import net.minecraft.client.renderer.GLAllocation;

/**
 * Utility class that has static methods to help loading/encoding, etc.
 *
 * @author WorldSEnder
 */
public class Utils {
    private static CharsetDecoder utf8Decoder = Charset.forName("UTF-8").newDecoder()
            .onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT);

    /**
     * Helper method for extending classes to read a null-terminated String in the BMP-unicode plane. THIS MEANS ONLY
     * UNICODE U+0001 - U+FFFF are supported.<br>
     * All characters from U+10000 must be present as surrogate characters ranging from U+D800 - U+DFFF.<br>
     * Refer to the Charset.forName("UTF-8")<br>
     * Decodes the next bytes to a {@link String}. The bytes are interpreted as valid UTF-8 data. Bytes are read until a
     * (null)-byte occurs (read inclusively) or the EOF is found.
     *
     * @param dis the {@link DataInputStream} to read from
     * @return the read {@link String}
     * @throws MalformedInputException      when a byte sequence can't be decoded
     * @throws UnmappableCharacterException when a byte sequence can't be mapped to a character
     * @throws IOException                  if an IOError occurs that is not an {@link EOFException}
     * @see
     */
    public static String readString(DataInputStream dis)
            throws MalformedInputException,
            UnmappableCharacterException,
            IOException {
        // Read bytes
        byte[] buffer = new byte[64];
        int currLength = 64;
        int offset = 0;
        for (int currByte; (currByte = dis.read()) > 0; ) {
            if (offset == currLength) {
                currLength += 64;
                buffer = Arrays.copyOf(buffer, currLength);
            }
            buffer[offset++] = (byte) currByte; // Fill and increase
        }
        // filledBuffer.length == offset
        // Return empty string
        if (offset == 0)
            return "";
        // Could use String(byte[], Charset) here but want some control over
        // fail-actions
        // Prepare
        utf8Decoder.reset();
        int strLen = 0;
        int maxLen = (int) Math.ceil(offset * utf8Decoder.maxCharsPerByte());
        char[] target = new char[maxLen];
        // Wrap in buffer
        ByteBuffer sourceBuff = ByteBuffer.wrap(buffer, 0, offset);
        CharBuffer targetBuff = CharBuffer.wrap(target);
        // Standard decoder procedure
        try {
            CoderResult cr = utf8Decoder.decode(sourceBuff, targetBuff, true);
            if (!cr.isUnderflow())
                cr.throwException();
            cr = utf8Decoder.flush(targetBuff);
            if (!cr.isUnderflow())
                cr.throwException();
        } catch (CharacterCodingException x) {
        } // Shouldn't happen
        strLen = targetBuff.position();
        return new String(target, 0, strLen);
    }

    /**
     * Reads a {@link Vector2f} from the datainput
     *
     * @param dis the {@link DataInputStream} to read from
     * @return the constructed {@link Vector3f}
     * @throws EOFException when the data ends before 3 floats are read
     * @throws IOException  when some IOException occurs in the given {@link DataInputStream}
     */
    public static Vector2f readVector2f(DataInputStream dis) throws EOFException, IOException {
        float x = dis.readFloat();
        float y = dis.readFloat();
        return new Vector2f(x, y);
    }

    /**
     * Reads a {@link Vector3f} from the datainput
     *
     * @param dis the {@link DataInputStream} to read from
     * @return the constructed {@link Vector3f}
     * @throws EOFException when the data ends before 3 floats are read
     * @throws IOException  when some IOException occurs in the given {@link DataInputStream}
     */
    public static Vector3f readVector3f(DataInputStream dis) throws EOFException, IOException {
        float x = dis.readFloat();
        float y = dis.readFloat();
        float z = dis.readFloat();
        return new Vector3f(x, y, z);
    }

    /**
     * Reads a {@link Quat4f} from the given {@link InputStream}.
     *
     * @param dis the {@link DataInputStream} to read from
     * @throws EOFException when the data ends before 4 floats are read
     * @throws IOException  when some IOException occurs in the given {@link DataInputStream}
     */
    public static Quat4f readQuat(DataInputStream dis) throws EOFException, IOException {
        float x = dis.readFloat();
        float y = dis.readFloat();
        float z = dis.readFloat();
        float w = dis.readFloat();
        return new Quat4f(x, y, z, w);
    }

    /**
     * Asks for a new directly backed ByteBuffer
     *
     * @param size the number of buffer elements (bytes)
     * @return a new direct byte buffer
     */
    public static ByteBuffer directByteBuffer(int size) {
        return GLAllocation.createDirectByteBuffer(size);
    }

    /**
     * Asks for a new directly backed IntBuffer
     *
     * @param size the number of buffer elements (ints)
     * @return a new direct int buffer
     */
    public static IntBuffer directIntBuffer(int size) {
        return GLAllocation.createDirectByteBuffer(size << 2).asIntBuffer();
    }

    /**
     * Asks for a new directly backed FloatBuffer
     *
     * @param size the number of buffer elements (floats)
     * @return a new direct float buffer
     */
    public static FloatBuffer directFloatBuffer(int size) {
        return GLAllocation.createDirectFloatBuffer(size);
    }

    /**
     * Asks for a new directly backed DoubleBuffer
     *
     * @param size the number of buffer elements (doubles)
     * @return a new direct float buffer
     */
    public static DoubleBuffer directDoubleBuffer(int size) {
        return directByteBuffer(size * 4).asDoubleBuffer();
    }

    /**
     * Asks for a new directly backed ShortBuffer
     *
     * @param size the number of buffer elements (shorts)
     * @return a new direct float buffer
     */
    public static ShortBuffer directShortBuffer(int size) {
        return directByteBuffer(size * 2).asShortBuffer();
    }

    /**
     * Gets the source in the {@link InputStream} as a direct byte buffer which can be supplied to OpenGL in
     * {@link GL20#glShaderSource(int, ByteBuffer)} to compile a shader. The inputStream will be read as long as it has
     * at least one byte. The read will behave as a blocking-read.
     *
     * @param is the {@link InputStream} to read the shader source from
     * @return a direct {@link ByteBuffer} containing the source
     * @throws IOException
     */
    public static ByteBuffer getShaderSource(InputStream is) throws IOException {
        ReadableByteChannel readChannel = Channels.newChannel(is);
        int bufferSize = 4096;
        ByteBuffer target = directByteBuffer(bufferSize);
        while (readChannel.read(target) > 0) {
            // If target is not full we reached an EOF
            if (target.hasRemaining())
                break;
            // Else increase the buffer size
            bufferSize += 4096;
            target.flip();
            target = directByteBuffer(bufferSize).put(target);
        }
        target.flip();
        return target;
    }

    /**
     * Compiles a new shader of tpye shaderTpye using the {@link InputStream} as shader source and returns the name of
     * the newly created shader.<br>
     * Returning 0 means that no shader could be created.
     *
     * @param shaderType the GL_TYPE of the shader like {@link GL20#GL_VERTEX_SHADER}
     * @param is         the {@link InputStream} to read the shader source from
     * @return the shadername
     * @throws IOException
     * @throws IllegalStateException when compiling of the shader fails.
     */
    public static int compileShaderSafe(int shaderType, InputStream is) throws IOException {
        int shaderName = glCreateShader(shaderType);
        if (shaderName == 0) {
            throw new IllegalStateException("GL Error: Created Shader is 0. Can't proceed.");
        }
        ByteBuffer shaderSource = getShaderSource(is);
        glShaderSource(shaderName, StandardCharsets.UTF_8.decode(shaderSource));
        glCompileShader(shaderName);
        if (glGetShaderi(shaderName, GL_COMPILE_STATUS) == GL_FALSE) {
            int errorLength = glGetShaderi(shaderName, GL_INFO_LOG_LENGTH);
            String error = glGetShaderInfoLog(shaderName, errorLength);
            glDeleteShader(shaderName); // Delete first as it will be out of
            // scope
            throw new IllegalStateException(error);
        }
        return shaderName;
    }

    /**
     * Creates a new GLprogram and returns it's name
     *
     * @return the new program's name
     * @throws IllegalStateException when no program could be created
     */
    public static int createProgramSafe() {
        int programName = glCreateProgram();
        if (programName == 0) {
            throw new IllegalStateException("GL Error: Created Program is 0. Can't proceed.");
        }
        return programName;
    }

    /**
     * Searches for a shader with the specified type on the given program object. Returns 0 when no such shader could be
     * found
     *
     * @param program      the programName to search
     * @param searchedType the GL-type-constant to search for
     * @return the found shader
     */
    public static int getShader(int program, int searchedType) {
        int shaderCount = glGetProgrami(program, GL_ATTACHED_SHADERS);
        IntBuffer attachedShaders = directIntBuffer(shaderCount);
        IntBuffer count = directIntBuffer(1);
        glGetAttachedShaders(program, count, attachedShaders);
        assert count.get() == shaderCount;
        for (int i = 0; i < shaderCount; ++i) {
            int shaderCandidate = attachedShaders.get();
            if (searchedType == glGetShaderi(shaderCandidate, GL_SHADER_TYPE)) {
                return shaderCandidate;
            }
        }
        return 0;
    }

    /**
     * Links the specified program and throws when the linking fails
     *
     * @param program the program to link
     * @throws IllegalStateException when the linking fails
     */
    public static void linkProgramSafe(int program) {
        glLinkProgram(program);
        if (glGetProgrami(program, GL_LINK_STATUS) == GL_FALSE) {
            int errorLength = glGetProgrami(program, GL_INFO_LOG_LENGTH);
            String error = glGetProgramInfoLog(program, errorLength);
            throw new IllegalStateException(error);
        }
    }

    /**
     * Takes a {@link Quaternion} and a {@link Vector3f} as input and returns a {@link Matrix4f} that represents the
     * rotation-translation of the passed in arguments.<br>
     * This handles null values as follows:<br>
     * - if rotation is <code>null</code> the upper left 3x3 matrix will be the identity matrix - if translation is
     * <code>null</code> the third column will be the identity vector
     */
    public static Matrix4f fromRTS(Quat4f rotation, Vector3f offset, Vector3f scale, Matrix4f out) {
        fromRTS(rotation, offset, 1.0f, out);
        float sX = scale.x, sY = scale.y, sZ = scale.z;
        out.m00 *= sX;
        out.m01 *= sX;
        out.m02 *= sX;

        out.m10 *= sY;
        out.m11 *= sY;
        out.m12 *= sY;

        out.m20 *= sZ;
        out.m21 *= sZ;
        out.m22 *= sZ;
        return out;
    }

    /**
     * Same as {@link #fromRTS(Quat4f, Vector3f, Vector3f)}, but with only one scaling component that is the same for x,
     * y and z
     *
     * @return
     * @see #fromRTS(Quat4f, Vector3f, Vector3f)
     */
    public static Matrix4f fromRTS(Quat4f rotation, Vector3f offset, float scale, Matrix4f out) {
        out.set(rotation, offset, scale);
        return out;
    }

    public static long asciiToMagicNumber(String asString) {
        byte[] asBytes = asString.getBytes();
        if (asBytes.length != 8) {
            throw new IllegalArgumentException(
                    "string must have a byte length of 8 but is \"" + asString + "\" (" + asBytes.length + ")");
        }
        return Longs.fromByteArray(asBytes);
    }
}
