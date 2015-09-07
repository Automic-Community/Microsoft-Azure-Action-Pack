package com.automic.azure.model;

import java.io.StringWriter;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * An POJO java class which maps to XML structure which is required to handle the error response.
 */

@XmlRootElement(name = "BlockList")
public class AzurePutBlockBlobList {

    private List<String> blockIdList;

    @XmlElement(name = "Latest")
    public List<String> getCode() {
        return blockIdList;
    }

    public void setCode(List<String> blocks) {
        this.blockIdList = blocks;
    }

    @Override
    public String toString() {
        StringBuilder responseBuilder = new StringBuilder("List of blocks:\n");
        for (String blockId : blockIdList) {
            responseBuilder.append(blockId);
        }
        return responseBuilder.toString();
    }

    /**
     * Method to get length of marshaled element
     * 
     * @return length of marshaled element
     * @throws JAXBException
     */
    public int getJaxbLength() throws JAXBException {
        StringWriter writer = new StringWriter();
        JAXBContext context = JAXBContext.newInstance(this.getClass());
        Marshaller m = context.createMarshaller();
        m.marshal(this, writer);
        String blockIdXml = writer.toString();
        return blockIdXml.length();
    }

}
