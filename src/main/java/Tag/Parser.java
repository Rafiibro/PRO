package Tag;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataFormatImpl;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Parser {

    private BufferedImage readImage(File file) throws IOException {
        ImageInputStream stream = null;
        BufferedImage image = null;
        try {
            stream = ImageIO.createImageInputStream(file);
            Iterator<ImageReader> readers = ImageIO.getImageReaders(stream);
            if (readers.hasNext()) {
                ImageReader reader = readers.next();
                reader.setInput(stream);
                image = reader.read(0);
            }
        } finally {
            if (stream != null) {
                stream.close();
            }
        }

        return image;
    }

    public void showTags(String imagesPath) {

        try {
            File fileOrDirectory = new File(imagesPath);

            if (fileOrDirectory.isFile()) {
                processFile(fileOrDirectory);
            } else {
                processDirectory(fileOrDirectory);
            }

            System.out.println("\nDone");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void setTags(ArrayList<String> tags, String imagesPath) {
        try {
            File inputFile = new File(imagesPath);
            if (inputFile.isFile()) {
                ImageInputStream input = ImageIO.createImageInputStream(inputFile);
                ImageOutputStream output = ImageIO.createImageOutputStream(inputFile);

               Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
               ImageReader reader = readers.next(); // TODO: Validate that there are readers

               reader.setInput(input);
               IIOImage image = reader.readAll(0, null);

               addTextEntry(image.getMetadata(), "heig", tags);

               ImageWriter writer = ImageIO.getImageWriter(reader); // TODO: Validate that there are writers
               writer.setOutput(output);
               writer.write(image);
            } else if(inputFile.isDirectory()) {
                File[] contents = inputFile.listFiles();
                for (File file : contents) {
                    if (file.isFile()) {
                        ImageInputStream input = ImageIO.createImageInputStream(file);
                        ImageOutputStream output = ImageIO.createImageOutputStream(file);

                       Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
                       ImageReader reader = readers.next(); // TODO: Validate that there are readers

                       reader.setInput(input);
                       IIOImage image = reader.readAll(0, null);

                       addTextEntry(image.getMetadata(), "heig", tags);

                       ImageWriter writer = ImageIO.getImageWriter(reader); // TODO: Validate that there are writers
                       writer.setOutput(output);
                       writer.write(image);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void indent(int level) {
        for (int i = 0; i < level; i++) {
            System.out.print("    ");
        }
    }

    private void displayAttributes(NamedNodeMap attributes) {
        if (attributes != null) {
            int count = attributes.getLength();
            for (int i = 0; i < count; i++) {
                Node attribute = attributes.item(i);

                System.out.print(" ");
                System.out.print(attribute.getNodeName());
                System.out.print("='");
                System.out.print(attribute.getNodeValue());
                System.out.print("'");
            }
        }
    }

    private void displayMetadataNode(Node node, int level) {
        indent(level);
        System.out.print("<");
        System.out.print(node.getNodeName());

        NamedNodeMap attributes = node.getAttributes();
        displayAttributes(attributes);

        Node child = node.getFirstChild();
        if (child == null) {
            String value = node.getNodeValue();
            if (value == null || value.length() == 0) {
                System.out.println("/>");
            } else {
                System.out.print(">");
                System.out.print(value);
                System.out.print("<");
                System.out.print(node.getNodeName());
                System.out.println(">");
            }
            return;
        }

        System.out.println(">");
        while (child != null) {
            displayMetadataNode(child, level + 1);
            child = child.getNextSibling();
        }

        indent(level);
        System.out.print("</");
        System.out.print(node.getNodeName());
        System.out.println(">");
    }

    private void dumpMetadata(IIOMetadata metadata) {
        String[] names = metadata.getMetadataFormatNames();
        int length = names.length;
        for (int i = 0; i < length; i++) {
            indent(2);
            System.out.println("Format name: " + names[i]);
            displayMetadataNode(metadata.getAsTree(names[i]), 3);
        }
    }

    private void processFileWithReader(File file, ImageReader reader) throws IOException {
        ImageInputStream stream = null;

        try {
            stream = ImageIO.createImageInputStream(file);

            reader.setInput(stream, true);

            IIOMetadata metadata = reader.getImageMetadata(0);

            indent(1);
            System.out.println("Image metadata");
            dumpMetadata(metadata);

            metadata = reader.getStreamMetadata();
            if (metadata != null) {
                indent(1);
                System.out.println("Stream metadata");
                dumpMetadata(metadata);
            }

        } finally {
            if (stream != null) {
                stream.close();
            }
        }
    }
    
    private String getFileExtension(File file) {
        String fileName = file.getName();
        int lastDot = fileName.lastIndexOf('.');
        return fileName.substring(lastDot + 1);
    }

    private void processFile(File file) throws IOException {
        System.out.println("\nProcessing " + file.getName() + ":\n");

        String extension = getFileExtension(file);

        Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName(extension);

        while (readers.hasNext()) {
            ImageReader reader = readers.next();

            System.out.println("Reader: " + reader.getClass().getName());

            processFileWithReader(file, reader);
        }
    }

    private void processDirectory(File directory) throws IOException {
        System.out.println("Processing all files in " + directory.getAbsolutePath());

        File[] contents = directory.listFiles();
        for (File file : contents) {
            if (file.isFile()) {
                processFile(file);
            }
        }
    }

    private void addTextEntry(final IIOMetadata metadata, final String key, final ArrayList<String> value) throws IIOInvalidTreeException {
        IIOMetadataNode text = new IIOMetadataNode("Text");
        
        for(String tag : value){
            IIOMetadataNode textEntry = new IIOMetadataNode("TextEntry");
            textEntry.setAttribute("keyword", key);
            textEntry.setAttribute("value", tag);
            text.appendChild(textEntry);
        }

        IIOMetadataNode root = new IIOMetadataNode(IIOMetadataFormatImpl.standardMetadataFormatName);
        root.appendChild(text);
        
        metadata.mergeTree(IIOMetadataFormatImpl.standardMetadataFormatName, root);
    }

    private String getTextEntry(final IIOMetadata metadata, final String key) {
        IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree(IIOMetadataFormatImpl.standardMetadataFormatName);
        NodeList entries = root.getElementsByTagName("TextEntry");

        for (int i = 0; i < entries.getLength(); i++) {
            IIOMetadataNode node = (IIOMetadataNode) entries.item(i);
            if (node.getAttribute("keyword").equals(key)) {
                return node.getAttribute("value");
            }
        }

        return null;
    }
}
