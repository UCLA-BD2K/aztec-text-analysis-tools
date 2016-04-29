/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucla.cs.scai.aztec;

import edu.ucla.cs.scai.aztec.utils.ImageUtils;
import java.util.Date;
import java.util.Objects;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class AztecEntry {

    String name;
    String description;
    String logo;
    String source;
    String language;
    String[] platforms;
    String sourceCodeURL;
    String[] linkDescriptions;
    String[] linkUrls;
    String[] institutions;
    String[] maintainers;
    String[] maintainerEmails;
    String[] types;
    String[] tags;
    String[] domains;
    Date[] dateCreated;
    Date[] dateUpdated;
    String[] owners;
    String id;
    String version;
    double[] logoHistogram;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String[] getPlatforms() {
        return platforms;
    }

    public void setPlatforms(String[] platforms) {
        this.platforms = platforms;
    }

    public String getSourceCodeURL() {
        return sourceCodeURL;
    }

    public void setSourceCodeURL(String sourceCodeURL) {
        this.sourceCodeURL = sourceCodeURL;
    }

    public String[] getLinkDescriptions() {
        return linkDescriptions;
    }

    public void setLinkDescriptions(String[] linkDescriptions) {
        this.linkDescriptions = linkDescriptions;
    }

    public String[] getLinkUrls() {
        return linkUrls;
    }

    public void setLinkUrls(String[] linkUrls) {
        this.linkUrls = linkUrls;
    }

    public String[] getInstitutions() {
        return institutions;
    }

    public void setInstitutions(String[] institutions) {
        this.institutions = institutions;
    }

    public String[] getMaintainers() {
        return maintainers;
    }

    public void setMaintainers(String[] maintainers) {
        this.maintainers = maintainers;
    }

    public String[] getMaintainerEmails() {
        return maintainerEmails;
    }

    public void setMaintainerEmails(String[] maintainerEmails) {
        this.maintainerEmails = maintainerEmails;
    }

    public String[] getTypes() {
        return types;
    }

    public void setTypes(String[] types) {
        this.types = types;
    }

    public String[] getTags() {
        return tags;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }

    public String[] getDomains() {
        return domains;
    }

    public void setDomains(String[] domains) {
        this.domains = domains;
    }

    public Date[] getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date[] dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Date[] getDateUpdated() {
        return dateUpdated;
    }

    public void setDateUpdated(Date[] dateUpdated) {
        this.dateUpdated = dateUpdated;
    }

    public String[] getOwners() {
        return owners;
    }

    public void setOwners(String[] owners) {
        this.owners = owners;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    private void computeLogoHistogram() throws Exception {
        logoHistogram = new ImageUtils().histogramFromImage(logo);
    }

    public double[] getLogoHistogram() {
        if (logoHistogram == null) {
            try {
                computeLogoHistogram();
            } catch (Exception e) {
                //e.printStackTrace();
            }
        }
        return logoHistogram;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 73 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AztecEntry other = (AztecEntry) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }

}
