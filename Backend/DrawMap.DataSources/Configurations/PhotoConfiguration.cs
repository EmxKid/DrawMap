using DrawMap.Domain;
using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;

namespace DrawMap.DataSources.Configurations;

public class PhotoConfiguration : IEntityTypeConfiguration<Photo>
{
    public void Configure(EntityTypeBuilder<Photo> builder)
    {
        builder.HasKey(p => p.Id);

        builder.Property(p => p.Id)
            .IsRequired();

        builder.Property(p => p.LocationId)
            .IsRequired(false);
    }
}