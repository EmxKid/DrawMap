using DrawMap.Domain;
using DrawMap.WebAPI.DTO;
using DomainRoute = DrawMap.Domain.Route;

namespace DrawMap.WebAPI.Services;

public interface IRouteService
{
    Task<DomainRoute> AddRoute(DomainRoute route, CancellationToken cancellationToken);
    Task<DomainRoute?> GetRoute(string routeId, CancellationToken cancellationToken);
    Task<List<DomainRoute>> GetRoutes(CancellationToken cancellationToken);
    Task<bool> DeleteRoute(string routeId, CancellationToken cancellationToken);
    Task<bool> UpdateRoute(string routeId, DomainRoute route, CancellationToken cancellationToken);
}
