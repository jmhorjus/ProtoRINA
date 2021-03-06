/**
 * @copyright 2013 Computer Science Department, Recursive InterNetworking Architecture (RINA) laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The RINA laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 * It is provided "as is" without express or implied warranty. 
 */

// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: enrollmentInformation_t.proto

package rina.object.gpb;

public final class EnrollmentInformation_t {
  private EnrollmentInformation_t() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
  }
  public interface enrollmentInformation_tOrBuilder
      extends com.google.protobuf.MessageOrBuilder {
    
    // optional uint64 address = 1;
    boolean hasAddress();
    long getAddress();
    
    // optional string operationalStatus = 2;
    boolean hasOperationalStatus();
    String getOperationalStatus();
  }
  public static final class enrollmentInformation_t extends
      com.google.protobuf.GeneratedMessage
      implements enrollmentInformation_tOrBuilder {
    // Use enrollmentInformation_t.newBuilder() to construct.
    private enrollmentInformation_t(Builder builder) {
      super(builder);
    }
    private enrollmentInformation_t(boolean noInit) {}
    
    private static final enrollmentInformation_t defaultInstance;
    public static enrollmentInformation_t getDefaultInstance() {
      return defaultInstance;
    }
    
    public enrollmentInformation_t getDefaultInstanceForType() {
      return defaultInstance;
    }
    
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return rina.object.gpb.EnrollmentInformation_t.internal_static_rina_messages_enrollmentInformation_t_descriptor;
    }
    
    protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return rina.object.gpb.EnrollmentInformation_t.internal_static_rina_messages_enrollmentInformation_t_fieldAccessorTable;
    }
    
    private int bitField0_;
    // optional uint64 address = 1;
    public static final int ADDRESS_FIELD_NUMBER = 1;
    private long address_;
    public boolean hasAddress() {
      return ((bitField0_ & 0x00000001) == 0x00000001);
    }
    public long getAddress() {
      return address_;
    }
    
    // optional string operationalStatus = 2;
    public static final int OPERATIONALSTATUS_FIELD_NUMBER = 2;
    private java.lang.Object operationalStatus_;
    public boolean hasOperationalStatus() {
      return ((bitField0_ & 0x00000002) == 0x00000002);
    }
    public String getOperationalStatus() {
      java.lang.Object ref = operationalStatus_;
      if (ref instanceof String) {
        return (String) ref;
      } else {
        com.google.protobuf.ByteString bs = 
            (com.google.protobuf.ByteString) ref;
        String s = bs.toStringUtf8();
        if (com.google.protobuf.Internal.isValidUtf8(bs)) {
          operationalStatus_ = s;
        }
        return s;
      }
    }
    private com.google.protobuf.ByteString getOperationalStatusBytes() {
      java.lang.Object ref = operationalStatus_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8((String) ref);
        operationalStatus_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    
    private void initFields() {
      address_ = 0L;
      operationalStatus_ = "";
    }
    private byte memoizedIsInitialized = -1;
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized != -1) return isInitialized == 1;
      
      memoizedIsInitialized = 1;
      return true;
    }
    
    public void writeTo(com.google.protobuf.CodedOutputStream output)
                        throws java.io.IOException {
      getSerializedSize();
      if (((bitField0_ & 0x00000001) == 0x00000001)) {
        output.writeUInt64(1, address_);
      }
      if (((bitField0_ & 0x00000002) == 0x00000002)) {
        output.writeBytes(2, getOperationalStatusBytes());
      }
      getUnknownFields().writeTo(output);
    }
    
    private int memoizedSerializedSize = -1;
    public int getSerializedSize() {
      int size = memoizedSerializedSize;
      if (size != -1) return size;
    
      size = 0;
      if (((bitField0_ & 0x00000001) == 0x00000001)) {
        size += com.google.protobuf.CodedOutputStream
          .computeUInt64Size(1, address_);
      }
      if (((bitField0_ & 0x00000002) == 0x00000002)) {
        size += com.google.protobuf.CodedOutputStream
          .computeBytesSize(2, getOperationalStatusBytes());
      }
      size += getUnknownFields().getSerializedSize();
      memoizedSerializedSize = size;
      return size;
    }
    
    private static final long serialVersionUID = 0L;
    @java.lang.Override
    protected java.lang.Object writeReplace()
        throws java.io.ObjectStreamException {
      return super.writeReplace();
    }
    
    public static rina.object.gpb.EnrollmentInformation_t.enrollmentInformation_t parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data).buildParsed();
    }
    public static rina.object.gpb.EnrollmentInformation_t.enrollmentInformation_t parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data, extensionRegistry)
               .buildParsed();
    }
    public static rina.object.gpb.EnrollmentInformation_t.enrollmentInformation_t parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data).buildParsed();
    }
    public static rina.object.gpb.EnrollmentInformation_t.enrollmentInformation_t parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data, extensionRegistry)
               .buildParsed();
    }
    public static rina.object.gpb.EnrollmentInformation_t.enrollmentInformation_t parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input).buildParsed();
    }
    public static rina.object.gpb.EnrollmentInformation_t.enrollmentInformation_t parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input, extensionRegistry)
               .buildParsed();
    }
    public static rina.object.gpb.EnrollmentInformation_t.enrollmentInformation_t parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      Builder builder = newBuilder();
      if (builder.mergeDelimitedFrom(input)) {
        return builder.buildParsed();
      } else {
        return null;
      }
    }
    public static rina.object.gpb.EnrollmentInformation_t.enrollmentInformation_t parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      Builder builder = newBuilder();
      if (builder.mergeDelimitedFrom(input, extensionRegistry)) {
        return builder.buildParsed();
      } else {
        return null;
      }
    }
    public static rina.object.gpb.EnrollmentInformation_t.enrollmentInformation_t parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input).buildParsed();
    }
    public static rina.object.gpb.EnrollmentInformation_t.enrollmentInformation_t parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input, extensionRegistry)
               .buildParsed();
    }
    
    public static Builder newBuilder() { return Builder.create(); }
    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder(rina.object.gpb.EnrollmentInformation_t.enrollmentInformation_t prototype) {
      return newBuilder().mergeFrom(prototype);
    }
    public Builder toBuilder() { return newBuilder(this); }
    
    @java.lang.Override
    protected Builder newBuilderForType(
        com.google.protobuf.GeneratedMessage.BuilderParent parent) {
      Builder builder = new Builder(parent);
      return builder;
    }
    public static final class Builder extends
        com.google.protobuf.GeneratedMessage.Builder<Builder>
       implements rina.object.gpb.EnrollmentInformation_t.enrollmentInformation_tOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return rina.object.gpb.EnrollmentInformation_t.internal_static_rina_messages_enrollmentInformation_t_descriptor;
      }
      
      protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
          internalGetFieldAccessorTable() {
        return rina.object.gpb.EnrollmentInformation_t.internal_static_rina_messages_enrollmentInformation_t_fieldAccessorTable;
      }
      
      // Construct using rina.messages.EnrollmentInformationT.enrollmentInformation_t.newBuilder()
      private Builder() {
        maybeForceBuilderInitialization();
      }
      
      private Builder(BuilderParent parent) {
        super(parent);
        maybeForceBuilderInitialization();
      }
      private void maybeForceBuilderInitialization() {
        if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
        }
      }
      private static Builder create() {
        return new Builder();
      }
      
      public Builder clear() {
        super.clear();
        address_ = 0L;
        bitField0_ = (bitField0_ & ~0x00000001);
        operationalStatus_ = "";
        bitField0_ = (bitField0_ & ~0x00000002);
        return this;
      }
      
      public Builder clone() {
        return create().mergeFrom(buildPartial());
      }
      
      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return rina.object.gpb.EnrollmentInformation_t.enrollmentInformation_t.getDescriptor();
      }
      
      public rina.object.gpb.EnrollmentInformation_t.enrollmentInformation_t getDefaultInstanceForType() {
        return rina.object.gpb.EnrollmentInformation_t.enrollmentInformation_t.getDefaultInstance();
      }
      
      public rina.object.gpb.EnrollmentInformation_t.enrollmentInformation_t build() {
        rina.object.gpb.EnrollmentInformation_t.enrollmentInformation_t result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }
      
      private rina.object.gpb.EnrollmentInformation_t.enrollmentInformation_t buildParsed()
          throws com.google.protobuf.InvalidProtocolBufferException {
        rina.object.gpb.EnrollmentInformation_t.enrollmentInformation_t result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(
            result).asInvalidProtocolBufferException();
        }
        return result;
      }
      
      public rina.object.gpb.EnrollmentInformation_t.enrollmentInformation_t buildPartial() {
        rina.object.gpb.EnrollmentInformation_t.enrollmentInformation_t result = new rina.object.gpb.EnrollmentInformation_t.enrollmentInformation_t(this);
        int from_bitField0_ = bitField0_;
        int to_bitField0_ = 0;
        if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
          to_bitField0_ |= 0x00000001;
        }
        result.address_ = address_;
        if (((from_bitField0_ & 0x00000002) == 0x00000002)) {
          to_bitField0_ |= 0x00000002;
        }
        result.operationalStatus_ = operationalStatus_;
        result.bitField0_ = to_bitField0_;
        onBuilt();
        return result;
      }
      
      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof rina.object.gpb.EnrollmentInformation_t.enrollmentInformation_t) {
          return mergeFrom((rina.object.gpb.EnrollmentInformation_t.enrollmentInformation_t)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }
      
      public Builder mergeFrom(rina.object.gpb.EnrollmentInformation_t.enrollmentInformation_t other) {
        if (other == rina.object.gpb.EnrollmentInformation_t.enrollmentInformation_t.getDefaultInstance()) return this;
        if (other.hasAddress()) {
          setAddress(other.getAddress());
        }
        if (other.hasOperationalStatus()) {
          setOperationalStatus(other.getOperationalStatus());
        }
        this.mergeUnknownFields(other.getUnknownFields());
        return this;
      }
      
      public final boolean isInitialized() {
        return true;
      }
      
      public Builder mergeFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        com.google.protobuf.UnknownFieldSet.Builder unknownFields =
          com.google.protobuf.UnknownFieldSet.newBuilder(
            this.getUnknownFields());
        while (true) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              this.setUnknownFields(unknownFields.build());
              onChanged();
              return this;
            default: {
              if (!parseUnknownField(input, unknownFields,
                                     extensionRegistry, tag)) {
                this.setUnknownFields(unknownFields.build());
                onChanged();
                return this;
              }
              break;
            }
            case 8: {
              bitField0_ |= 0x00000001;
              address_ = input.readUInt64();
              break;
            }
            case 18: {
              bitField0_ |= 0x00000002;
              operationalStatus_ = input.readBytes();
              break;
            }
          }
        }
      }
      
      private int bitField0_;
      
      // optional uint64 address = 1;
      private long address_ ;
      public boolean hasAddress() {
        return ((bitField0_ & 0x00000001) == 0x00000001);
      }
      public long getAddress() {
        return address_;
      }
      public Builder setAddress(long value) {
        bitField0_ |= 0x00000001;
        address_ = value;
        onChanged();
        return this;
      }
      public Builder clearAddress() {
        bitField0_ = (bitField0_ & ~0x00000001);
        address_ = 0L;
        onChanged();
        return this;
      }
      
      // optional string operationalStatus = 2;
      private java.lang.Object operationalStatus_ = "";
      public boolean hasOperationalStatus() {
        return ((bitField0_ & 0x00000002) == 0x00000002);
      }
      public String getOperationalStatus() {
        java.lang.Object ref = operationalStatus_;
        if (!(ref instanceof String)) {
          String s = ((com.google.protobuf.ByteString) ref).toStringUtf8();
          operationalStatus_ = s;
          return s;
        } else {
          return (String) ref;
        }
      }
      public Builder setOperationalStatus(String value) {
        if (value == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000002;
        operationalStatus_ = value;
        onChanged();
        return this;
      }
      public Builder clearOperationalStatus() {
        bitField0_ = (bitField0_ & ~0x00000002);
        operationalStatus_ = getDefaultInstance().getOperationalStatus();
        onChanged();
        return this;
      }
      void setOperationalStatus(com.google.protobuf.ByteString value) {
        bitField0_ |= 0x00000002;
        operationalStatus_ = value;
        onChanged();
      }
      
      // @@protoc_insertion_point(builder_scope:rina.messages.enrollmentInformation_t)
    }
    
    static {
      defaultInstance = new enrollmentInformation_t(true);
      defaultInstance.initFields();
    }
    
    // @@protoc_insertion_point(class_scope:rina.messages.enrollmentInformation_t)
  }
  
  private static com.google.protobuf.Descriptors.Descriptor
    internal_static_rina_messages_enrollmentInformation_t_descriptor;
  private static
    com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internal_static_rina_messages_enrollmentInformation_t_fieldAccessorTable;
  
  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\035enrollmentInformation_t.proto\022\rrina.me" +
      "ssages\"E\n\027enrollmentInformation_t\022\017\n\007add" +
      "ress\030\001 \001(\004\022\031\n\021operationalStatus\030\002 \001(\t"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
      new com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner() {
        public com.google.protobuf.ExtensionRegistry assignDescriptors(
            com.google.protobuf.Descriptors.FileDescriptor root) {
          descriptor = root;
          internal_static_rina_messages_enrollmentInformation_t_descriptor =
            getDescriptor().getMessageTypes().get(0);
          internal_static_rina_messages_enrollmentInformation_t_fieldAccessorTable = new
            com.google.protobuf.GeneratedMessage.FieldAccessorTable(
              internal_static_rina_messages_enrollmentInformation_t_descriptor,
              new java.lang.String[] { "Address", "OperationalStatus", },
              rina.object.gpb.EnrollmentInformation_t.enrollmentInformation_t.class,
              rina.object.gpb.EnrollmentInformation_t.enrollmentInformation_t.Builder.class);
          return null;
        }
      };
    com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
        }, assigner);
  }
  
  // @@protoc_insertion_point(outer_class_scope)
}
