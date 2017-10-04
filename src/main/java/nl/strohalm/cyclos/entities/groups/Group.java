/*
    This file is part of Cyclos (www.cyclos.org).
    A project of the Social Trade Organisation (www.socialtrade.org).

    Cyclos is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    Cyclos is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Cyclos; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

 */
package nl.strohalm.cyclos.entities.groups;

import nl.strohalm.cyclos.access.Permission;
import nl.strohalm.cyclos.entities.Entity;
import nl.strohalm.cyclos.entities.Relationship;
import nl.strohalm.cyclos.entities.accounts.guarantees.GuaranteeType;
import nl.strohalm.cyclos.entities.accounts.transactions.PaymentFilter;
import nl.strohalm.cyclos.entities.accounts.transactions.TransferType;
import nl.strohalm.cyclos.entities.customization.files.CustomizedFile;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.entities.members.records.MemberRecordType;
import nl.strohalm.cyclos.entities.members.remarks.GroupRemark;
import nl.strohalm.cyclos.utils.StringValuedEnum;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.Converter;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * A group of permissions
 * @author luis
 */
@Table(name = "groups")
@DiscriminatorColumn(name = "subclass", length = 1)
@javax.persistence.Entity
public abstract class Group extends Entity implements Comparable<Group> {

    public static enum Nature {
        ADMIN("A"), MEMBER("M"), BROKER("B"), OPERATOR("O");

        private final String discriminator;

        private Nature(final String discriminator) {
            this.discriminator = discriminator;
        }

        public String getDiscriminator() {
            return discriminator;
        }

        public Element.Nature getElementNature() {
            switch (this) {
                case ADMIN:
                    return Element.Nature.ADMIN;
                case MEMBER:
                case BROKER:
                    return Element.Nature.MEMBER;
                case OPERATOR:
                    return Element.Nature.OPERATOR;
                default:
                    return null;
            }
        }

        public Class<? extends Group> getGroupClass() {
            switch (this) {
                case ADMIN:
                    return AdminGroup.class;
                case MEMBER:
                    return MemberGroup.class;
                case BROKER:
                    return BrokerGroup.class;
                case OPERATOR:
                    return OperatorGroup.class;
                default:
                    return null;
            }
        }
    }

    public static enum Relationships implements Relationship {
        ELEMENTS("elements"), PAYMENT_FILTERS("paymentFilters"), GROUP_FILTERS("groupFilters"), PERMISSIONS("permissions"), TRANSFER_TYPES("transferTypes"), CONVERSION_SIMULATION_TTS("conversionSimulationTTs"), CUSTOMIZED_FILES("customizedFiles"), GUARANTEE_TYPES("guaranteeTypes");

        private final String name;

        private Relationships(final String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }
    }

    public static enum Status implements StringValuedEnum {
        NORMAL("N"), REMOVED("R");
        private final String value;

        private Status(final String value) {
            this.value = value;
        }

        @Override
        public String getValue() {
            return value;
        }

        public boolean isEnabled() {
            return this == NORMAL;
        }
    }

    private static final long            serialVersionUID = 3079265000327578016L;

    @Column(name = "description", columnDefinition = "longtext")
    private String                       description;

    @OneToMany(mappedBy = "group")
	private Collection<Element>          elements;

    @Column(name = "name", nullable = false, length = 100)
    private String                       name;

    @ManyToMany
    @JoinTable(name = "groups_payment_filters",
        joinColumns = @JoinColumn(name = "group_id"),
        inverseJoinColumns = @JoinColumn(name = "payment_filter_id"))
	private Collection<PaymentFilter>    paymentFilters;

    @ElementCollection
    @CollectionTable(name = "permissions", joinColumns = @JoinColumn(name = "group_id"))
    @Column(name = "permission", nullable = false)
	private Collection<Permission>       permissions;

    @Column(name = "status", nullable = false, updatable = false, length = 1)
	private Status                       status           = Status.NORMAL;

    @Embedded
	private BasicGroupSettings           basicSettings    = new BasicGroupSettings();

    @ManyToMany
    @JoinTable(name = "groups_transfer_types",
            inverseJoinColumns = @JoinColumn(name = "transfer_type_id"),
            joinColumns = @JoinColumn(name = "group_id")
    )
	private Collection<TransferType>     transferTypes;

    @ManyToMany
    @JoinTable(name = "groups_conversion_simulation_transfer_types",
            joinColumns = @JoinColumn(name = "group_id"),
            inverseJoinColumns = @JoinColumn(name = "transfer_type_id"))
	private Collection<TransferType>     conversionSimulationTTs;

    @OneToMany(mappedBy = "group", cascade = CascadeType.REMOVE)
	private Collection<CustomizedFile>   customizedFiles;

    @ManyToMany(mappedBy = "groups")
	private Collection<GroupFilter>      groupFilters;

    @ManyToMany(mappedBy = "groups")
	private Collection<MemberRecordType> memberRecordTypes;

    @ManyToMany
    @JoinTable(name = "group_guarantee_types",
            joinColumns = @JoinColumn(name = "group_id"),
            inverseJoinColumns = @JoinColumn(name = "guarantee_type_id"))
	private Collection<GuaranteeType>    guaranteeTypes;

    @OneToMany(mappedBy = "group", cascade = CascadeType.REMOVE)
	private Collection<GroupHistoryLog>  historyLogs;

    @OneToMany(mappedBy = "newGroup", cascade = CascadeType.REMOVE)
	private Collection<GroupRemark>      oldRemarks;

    @OneToMany(mappedBy = "oldGroup", cascade = CascadeType.REMOVE)
	private Collection<GroupRemark>      newRemarks;

    protected Group() {
	}

	@Override
    public int compareTo(final Group o) {
        return name.compareTo(o.getName());
    }

    public BasicGroupSettings getBasicSettings() {
        return basicSettings;
    }

    public Collection<TransferType> getConversionSimulationTTs() {
        return conversionSimulationTTs;
    }

    public Collection<CustomizedFile> getCustomizedFiles() {
        return customizedFiles;
    }

    public String getDescription() {
        return description;
    }

    public Collection<Element> getElements() {
        return elements;
    }

    public Collection<GuaranteeType> getEnabledGuaranteeTypes() {
        final Collection<GuaranteeType> all = getGuaranteeTypes();

        if (all == null || all.isEmpty()) {
            return Collections.emptyList();
        }

        final ArrayList<GuaranteeType> enabled = new ArrayList<GuaranteeType>();
        for (final GuaranteeType gt : all) {
            if (gt.isEnabled()) {
                enabled.add(gt);
            }
        }

        return enabled;
    }

    public Collection<GroupFilter> getGroupFilters() {
        return groupFilters;
    }

    public Collection<GuaranteeType> getGuaranteeTypes() {
        return guaranteeTypes;
    }

    public Collection<GroupHistoryLog> getHistoryLogs() {
        return historyLogs;
    }

    public Collection<MemberRecordType> getMemberRecordTypes() {
        return memberRecordTypes;
    }

    @Override
    public String getName() {
        return name;
    }

    public abstract Nature getNature();

    public Collection<GroupRemark> getNewRemarks() {
        return newRemarks;
    }

    public Collection<GroupRemark> getOldRemarks() {
        return oldRemarks;
    }

    public Collection<PaymentFilter> getPaymentFilters() {
        return paymentFilters;
    }

    public Collection<Permission> getPermissions() {
        return permissions;
    }

    public Status getStatus() {
        return status;
    }

    public Collection<TransferType> getTransferTypes() {
        return transferTypes;
    }

    public boolean isRemoved() {
        return status == Status.REMOVED ? true : false;
    }

    public void setBasicSettings(BasicGroupSettings basicSettings) {
        if (basicSettings == null) {
            basicSettings = new BasicGroupSettings();
        }
        this.basicSettings = basicSettings;
    }

    public void setConversionSimulationTTs(final Collection<TransferType> conversionSimulationTTs) {
        this.conversionSimulationTTs = conversionSimulationTTs;
    }

    public void setCustomizedFiles(final Collection<CustomizedFile> customizedFiles) {
        this.customizedFiles = customizedFiles;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public void setElements(final Collection<Element> elements) {
        this.elements = elements;
    }

    public void setGroupFilters(final Collection<GroupFilter> groupFilters) {
        this.groupFilters = groupFilters;
    }

    public void setGuaranteeTypes(final Collection<GuaranteeType> guaranteeTypes) {
        this.guaranteeTypes = guaranteeTypes;
    }

    public void setHistoryLogs(final Collection<GroupHistoryLog> historyLogs) {
        this.historyLogs = historyLogs;
    }

    public void setMemberRecordTypes(final Collection<MemberRecordType> memberRecordTypes) {
        this.memberRecordTypes = memberRecordTypes;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setNewRemarks(final Collection<GroupRemark> newRemarks) {
        this.newRemarks = newRemarks;
    }

    public void setOldRemarks(final Collection<GroupRemark> oldRemarks) {
        this.oldRemarks = oldRemarks;
    }

    public void setPaymentFilters(final Collection<PaymentFilter> paymentFilters) {
        this.paymentFilters = paymentFilters;
    }

    public void setPermissions(final Collection<Permission> permissions) {
        this.permissions = permissions;
    }

    public void setStatus(final Status status) {
        this.status = status;
    }

    public void setTransferTypes(final Collection<TransferType> transferTypes) {
        this.transferTypes = transferTypes;
    }

    @Override
    public String toString() {
        return getId() + " - " + name;
    }

}
