package com.rtlis.core.storage.disk;

import com.rtlis.core.model.Point;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class SSTableWriter {
    public void write(List<Point> points, String filePath) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(filePath);
             FileChannel channel = fos.getChannel()) {
            for (Point p : points) {
                byte[] idBytes = p.getVehicleId().getBytes(StandardCharsets.UTF_8);

                int totalBytes = 4 + idBytes.length + 8 + 8 + 8;
                ByteBuffer buffer = ByteBuffer.allocate(totalBytes);

                //-- we write the data in the order we added their bytes
                buffer.putInt(idBytes.length); //-- 4 bytes: length of vehicleId
                buffer.put(idBytes); //-- N bytes - vehicleId in UTF-8
                buffer.putDouble(p.getLongitude()); //-- 8 bytes longitude
                buffer.putDouble(p.getLatitude()); //-- 8 bytes latitude
                buffer.putLong(p.getTimestamp()); //-- 8 bytes, timestamp

                buffer.flip(); //-- we flip/prepare the buffer to reading mode for reading by the channel
                channel.write(buffer); //-- we write all bytes to disk
            }
        }
    }
}
