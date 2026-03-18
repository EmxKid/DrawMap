using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Design;
using Microsoft.Extensions.Configuration;

namespace DrawMap.DataSources;

public class DrawMapDbContextFactory : IDesignTimeDbContextFactory<DrawMapDbContext>
{
    public DrawMapDbContext CreateDbContext(string[] args)
    {
        var optionsBuilder = new DbContextOptionsBuilder<DrawMapDbContext>();
        
        // Строка подключения для дизайн-тайма
        var connectionString = "Host=localhost;Port=5432;Database=drawmap;Username=postgres;Password=postgres";
        
        optionsBuilder.UseNpgsql(connectionString);

        return new DrawMapDbContext(optionsBuilder.Options);
    }
}
