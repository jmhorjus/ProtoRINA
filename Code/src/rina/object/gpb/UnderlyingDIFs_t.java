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
// source: underlyingDIFs.proto

package rina.object.gpb;

public final class UnderlyingDIFs_t {
  private UnderlyingDIFs_t() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
  }
  public interface underlyingDIFs_tOrBuilder
      extends com.google.protobuf.MessageOrBuilder {
    
    // repeated string underlyingDIFs = 1;
    java.util.List<String> getUnderlyingDIFsList();
    int getUnderlyingDIFsCount();
    String getUnderlyingDIFs(int index);
  }
  public static final class underlyingDIFs_t extends
      com.google.protobuf.GeneratedMessage
      implements underlyingDIFs_tOrBuilder {
    // Use underlyingDIFs_t.newBuilder() to construct.
    private underlyingDIFs_t(Builder builder) {
      super(builder);
    }
    private underlyingDIFs_t(boolean noInit) {}
    
    private static final underlyingDIFs_t defaultInstance;
    public static underlyingDIFs_t getDefaultInstance() {
      return defaultInstance;
    }
    
    public underlyingDIFs_t getDefaultInstanceForType() {
      return defaultInstance;
    }
    
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return rina.object.gpb.UnderlyingDIFs_t.internal_static_rina_object_gpb_underlyingDIFs_t_descriptor;
    }
    
    protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return rina.object.gpb.UnderlyingDIFs_t.internal_static_rina_object_gpb_underlyingDIFs_t_fieldAccessorTable;
    }
    
    // repeated string underlyingDIFs = 1;
    public static final int UNDERLYINGDIFS_FIELD_NUMBER = 1;
    private com.google.protobuf.LazyStringList underlyingDIFs_;
    public java.util.List<String>
        getUnderlyingDIFsList() {
      return underlyingDIFs_;
    }
    public int getUnderlyingDIFsCount() {
      return underlyingDIFs_.size();
    }
    public String getUnderlyingDIFs(int index) {
      return underlyingDIFs_.get(index);
    }
    
    private void initFields() {
      underlyingDIFs_ = com.google.protobuf.LazyStringArrayList.EMPTY;
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
      for (int i = 0; i < underlyingDIFs_.size(); i++) {
        output.writeBytes(1, underlyingDIFs_.getByteString(i));
      }
      getUnknownFields().writeTo(output);
    }
    
    private int memoizedSerializedSize = -1;
    public int getSerializedSize() {
      int size = memoizedSerializedSize;
      if (size != -1) return size;
    
      size = 0;
      {
        int dataSize = 0;
        for (int i = 0; i < underlyingDIFs_.size(); i++) {
          dataSize += com.google.protobuf.CodedOutputStream
            .computeBytesSizeNoTag(underlyingDIFs_.getByteString(i));
        }
        size += dataSize;
        size += 1 * getUnderlyingDIFsList().size();
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
    
    public static rina.object.gpb.UnderlyingDIFs_t.underlyingDIFs_t parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data).buildParsed();
    }
    public static rina.object.gpb.UnderlyingDIFs_t.underlyingDIFs_t parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data, extensionRegistry)
               .buildParsed();
    }
    public static rina.object.gpb.UnderlyingDIFs_t.underlyingDIFs_t parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data).buildParsed();
    }
    public static rina.object.gpb.UnderlyingDIFs_t.underlyingDIFs_t parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data, extensionRegistry)
               .buildParsed();
    }
    public static rina.object.gpb.UnderlyingDIFs_t.underlyingDIFs_t parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input).buildParsed();
    }
    public static rina.object.gpb.UnderlyingDIFs_t.underlyingDIFs_t parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input, extensionRegistry)
               .buildParsed();
    }
    public static rina.object.gpb.UnderlyingDIFs_t.underlyingDIFs_t parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      Builder builder = newBuilder();
      if (builder.mergeDelimitedFrom(input)) {
        return builder.buildParsed();
      } else {
        return null;
      }
    }
    public static rina.object.gpb.UnderlyingDIFs_t.underlyingDIFs_t parseDelimitedFrom(
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
    public static rina.object.gpb.UnderlyingDIFs_t.underlyingDIFs_t parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input).buildParsed();
    }
    public static rina.object.gpb.UnderlyingDIFs_t.underlyingDIFs_t parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input, extensionRegistry)
               .buildParsed();
    }
    
    public static Builder newBuilder() { return Builder.create(); }
    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder(rina.object.gpb.UnderlyingDIFs_t.underlyingDIFs_t prototype) {
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
       implements rina.object.gpb.UnderlyingDIFs_t.underlyingDIFs_tOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return rina.object.gpb.UnderlyingDIFs_t.internal_static_rina_object_gpb_underlyingDIFs_t_descriptor;
      }
      
      protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
          internalGetFieldAccessorTable() {
        return rina.object.gpb.UnderlyingDIFs_t.internal_static_rina_object_gpb_underlyingDIFs_t_fieldAccessorTable;
      }
      
      // Construct using rina.object.gpb.UnderlyingDIFs.underlyingDIFs_t.newBuilder()
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
        underlyingDIFs_ = com.google.protobuf.LazyStringArrayList.EMPTY;
        bitField0_ = (bitField0_ & ~0x00000001);
        return this;
      }
      
      public Builder clone() {
        return create().mergeFrom(buildPartial());
      }
      
      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return rina.object.gpb.UnderlyingDIFs_t.underlyingDIFs_t.getDescriptor();
      }
      
      public rina.object.gpb.UnderlyingDIFs_t.underlyingDIFs_t getDefaultInstanceForType() {
        return rina.object.gpb.UnderlyingDIFs_t.underlyingDIFs_t.getDefaultInstance();
      }
      
      public rina.object.gpb.UnderlyingDIFs_t.underlyingDIFs_t build() {
        rina.object.gpb.UnderlyingDIFs_t.underlyingDIFs_t result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }
      
      private rina.object.gpb.UnderlyingDIFs_t.underlyingDIFs_t buildParsed()
          throws com.google.protobuf.InvalidProtocolBufferException {
        rina.object.gpb.UnderlyingDIFs_t.underlyingDIFs_t result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(
            result).asInvalidProtocolBufferException();
        }
        return result;
      }
      
      public rina.object.gpb.UnderlyingDIFs_t.underlyingDIFs_t buildPartial() {
        rina.object.gpb.UnderlyingDIFs_t.underlyingDIFs_t result = new rina.object.gpb.UnderlyingDIFs_t.underlyingDIFs_t(this);
        int from_bitField0_ = bitField0_;
        if (((bitField0_ & 0x00000001) == 0x00000001)) {
          underlyingDIFs_ = new com.google.protobuf.UnmodifiableLazyStringList(
              underlyingDIFs_);
          bitField0_ = (bitField0_ & ~0x00000001);
        }
        result.underlyingDIFs_ = underlyingDIFs_;
        onBuilt();
        return result;
      }
      
      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof rina.object.gpb.UnderlyingDIFs_t.underlyingDIFs_t) {
          return mergeFrom((rina.object.gpb.UnderlyingDIFs_t.underlyingDIFs_t)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }
      
      public Builder mergeFrom(rina.object.gpb.UnderlyingDIFs_t.underlyingDIFs_t other) {
        if (other == rina.object.gpb.UnderlyingDIFs_t.underlyingDIFs_t.getDefaultInstance()) return this;
        if (!other.underlyingDIFs_.isEmpty()) {
          if (underlyingDIFs_.isEmpty()) {
            underlyingDIFs_ = other.underlyingDIFs_;
            bitField0_ = (bitField0_ & ~0x00000001);
          } else {
            ensureUnderlyingDIFsIsMutable();
            underlyingDIFs_.addAll(other.underlyingDIFs_);
          }
          onChanged();
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
            case 10: {
              ensureUnderlyingDIFsIsMutable();
              underlyingDIFs_.add(input.readBytes());
              break;
            }
          }
        }
      }
      
      private int bitField0_;
      
      // repeated string underlyingDIFs = 1;
      private com.google.protobuf.LazyStringList underlyingDIFs_ = com.google.protobuf.LazyStringArrayList.EMPTY;
      private void ensureUnderlyingDIFsIsMutable() {
        if (!((bitField0_ & 0x00000001) == 0x00000001)) {
          underlyingDIFs_ = new com.google.protobuf.LazyStringArrayList(underlyingDIFs_);
          bitField0_ |= 0x00000001;
         }
      }
      public java.util.List<String>
          getUnderlyingDIFsList() {
        return java.util.Collections.unmodifiableList(underlyingDIFs_);
      }
      public int getUnderlyingDIFsCount() {
        return underlyingDIFs_.size();
      }
      public String getUnderlyingDIFs(int index) {
        return underlyingDIFs_.get(index);
      }
      public Builder setUnderlyingDIFs(
          int index, String value) {
        if (value == null) {
    throw new NullPointerException();
  }
  ensureUnderlyingDIFsIsMutable();
        underlyingDIFs_.set(index, value);
        onChanged();
        return this;
      }
      public Builder addUnderlyingDIFs(String value) {
        if (value == null) {
    throw new NullPointerException();
  }
  ensureUnderlyingDIFsIsMutable();
        underlyingDIFs_.add(value);
        onChanged();
        return this;
      }
      public Builder addAllUnderlyingDIFs(
          java.lang.Iterable<String> values) {
        ensureUnderlyingDIFsIsMutable();
        super.addAll(values, underlyingDIFs_);
        onChanged();
        return this;
      }
      public Builder clearUnderlyingDIFs() {
        underlyingDIFs_ = com.google.protobuf.LazyStringArrayList.EMPTY;
        bitField0_ = (bitField0_ & ~0x00000001);
        onChanged();
        return this;
      }
      void addUnderlyingDIFs(com.google.protobuf.ByteString value) {
        ensureUnderlyingDIFsIsMutable();
        underlyingDIFs_.add(value);
        onChanged();
      }
      
      // @@protoc_insertion_point(builder_scope:rina.object.gpb.underlyingDIFs_t)
    }
    
    static {
      defaultInstance = new underlyingDIFs_t(true);
      defaultInstance.initFields();
    }
    
    // @@protoc_insertion_point(class_scope:rina.object.gpb.underlyingDIFs_t)
  }
  
  private static com.google.protobuf.Descriptors.Descriptor
    internal_static_rina_object_gpb_underlyingDIFs_t_descriptor;
  private static
    com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internal_static_rina_object_gpb_underlyingDIFs_t_fieldAccessorTable;
  
  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\024underlyingDIFs.proto\022\017rina.object.gpb\"" +
      "*\n\020underlyingDIFs_t\022\026\n\016underlyingDIFs\030\001 " +
      "\003(\t"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
      new com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner() {
        public com.google.protobuf.ExtensionRegistry assignDescriptors(
            com.google.protobuf.Descriptors.FileDescriptor root) {
          descriptor = root;
          internal_static_rina_object_gpb_underlyingDIFs_t_descriptor =
            getDescriptor().getMessageTypes().get(0);
          internal_static_rina_object_gpb_underlyingDIFs_t_fieldAccessorTable = new
            com.google.protobuf.GeneratedMessage.FieldAccessorTable(
              internal_static_rina_object_gpb_underlyingDIFs_t_descriptor,
              new java.lang.String[] { "UnderlyingDIFs", },
              rina.object.gpb.UnderlyingDIFs_t.underlyingDIFs_t.class,
              rina.object.gpb.UnderlyingDIFs_t.underlyingDIFs_t.Builder.class);
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
